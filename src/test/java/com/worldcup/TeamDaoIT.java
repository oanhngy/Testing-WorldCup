package com.worldcup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseIT {
    protected TeamDao teamDao;

    @BeforeAll
    void boot() throws Exception {
        SimpleDB.init();
        teamDao=new TeamDao();
    }

    @BeforeEach
    void truncateAll() throws Exception {
        //xóa data tránh fk
        try(Connection c=SimpleDB.get(); Statement st=c.createStatement()) {
            st.executeUpdate("delete from goals");
            st.executeUpdate("delete from substitutions");
            st.executeUpdate("delete from matches");
            st.executeUpdate("delete from group_teams");
            st.executeUpdate("delete from groups");
        }
        teamDao.deleteAll();
    }

    //==== HELPERS ====
    //chạy update sql
    protected int execUpdate(String sql,Object...args) throws Exception {
        try(Connection c=SimpleDB.get(); PreparedStatement ps=c.prepareStatement(sql)) {
            for(int i=0; i<args.length; i++) ps.setObject(i+1, args[i]);
            return ps.executeUpdate();
        }
    }

    //lấy id theo tên team
    protected long teamId(String name) throws Exception {
        try(Connection c=SimpleDB.get();
        PreparedStatement ps=c.prepareStatement("select ID from teams where name=?")) {
            ps.setString(1, name);
            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()) return rs.getLong(1);
            }
        }
        throw new IllegalStateException("Khong tim thay team "+ name+ "!");
    }

    //tạo group A, trả về id
    protected long createGroupA() throws Exception {
        try (Connection c = SimpleDB.get();
            PreparedStatement ps = c.prepareStatement(
                "insert into \"groups\"(name) values (?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "A");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new IllegalStateException("Khong tao duoc group A");
    }

    //gán team vào group
    protected void joinGroup(long groupId, long teamId) throws Exception {
        execUpdate("insert into group_teams(group_id, team_id) values(?,?)", groupId, teamId); 
    }

    //tạo lịch đấu
    protected void schedule(long groupId, long aId, long bId) throws Exception {
        execUpdate("insert into matches(group_id, team_a_id, team_b_id, status, is_knockout) values(?,?,?,?,0)", groupId, aId, bId, "SCHEDULED");
    }

    //update tỉ số 1 trận, tinh điểm,hiệu số cho team
    protected void playAndApply(long groupId, String aName, String bName, int ga, int gb) throws Exception {
        long aId=teamId(aName), bId=teamId(bName);
        //update tỉ số
        int rows=execUpdate("update matches set goals_a=?, goals_b=?, status='FINISHED'"+ "where group_id=? and team_a_id=? and team_b_id=?",
        ga,gb,groupId,aId,bId);
        if(rows==0) {
            rows=execUpdate("update matches set goals_a=?, goals_b=?, status='FINISHED'" + "where group_is=? and team_a_id=? and team_b_id=?",
            gb, ga, groupId, bId, aId);
            if(rows==0) {
                throw new IllegalStateException("Khong tim thay tran: "+ aName+" vs "+bName);
            }
        }
        //tính điểm
        int aPts=(ga>gb)?3:(ga==gb? 1:0);
        int bPts=(gb>ga)?3:(ga==gb? 1:0);
        int aGD=ga-gb;
        int bGD=gb-ga;
        execUpdate("update teams set points=points+?, goal_diff=goal_diff+? where id=?", aPts, aGD, aId);
        execUpdate("update teams set points=points+?, goal_diff=goal_diff+? where id=?", bPts, bGD, bId);
    }   
    //==== HELPERS ==== 
}


public class TeamDaoIT extends BaseIT {
    //smoke test db
    @Test
    void dbIsReady() throws Exception {
        Set<String> expected=Set.of("teams", "groups", "group_teams", "matches");
        Set<String> actual=new HashSet<>();
        try(Connection c=SimpleDB.get();
        PreparedStatement ps=c.prepareStatement("select name from sqlite_master where type='table'")) {
            try(ResultSet rs=ps.executeQuery()) {
                while(rs.next()) actual.add(rs.getString(1));
            }
        }
        assertTrue(actual.containsAll(expected), "Thieu bang "+diff(expected,actual));
    }

    private static Set<String> diff(Set<String> a, Set<String> b) {
        Set<String> d=new HashSet<>(a);
        d.removeAll(b);
        return d;
    }

    //CRUD, insert nhiều đội =CsvSource, find by name/id đúng
    @ParameterizedTest(name="CRUD: insert& fiind team ''{0}'' ({1})")
    @CsvSource({"Argentina,SA", "Brazil,SA", "Croatia,EU"})
    void teamCrudHappyPath(String name, String region) throws Exception {
        teamDao.deleteAll(); //xóa lần nữa
        Team t=new Team(name, region, "Coach", "Med");
        t.setHost(false);
        long id=teamDao.insert(t);
        assertTrue(id>0, "ID phai >0.");

        Team byId=teamDao.findById(id).orElseThrow();
        assertEquals(name, byId.getName());
        assertEquals(region, byId.getRegion());
        Team byName=teamDao.findByName(name).orElseThrow();
        assertEquals(id, byName.getId());
    }

    //E2E: tạo group A, 4 đội, 6 trận vòng bảng, chech điểm, hiệu số
    @Test
    void e2eGroupA_FullRound() throws Exception {
        long gA = createGroupA();

        // 4 đội
        teamDao.insert(new Team("Argentina", "SA", "A", "Med A"));
        teamDao.insert(new Team("Brazil",    "SA", "B", "Med B"));
        teamDao.insert(new Team("Croatia",   "EU", "C",   "Med C"));
        teamDao.insert(new Team("Japan",     "AS", "D","Med D"));

        // Gán vào group A
        long ARG = teamId("Argentina");
        long BRA = teamId("Brazil");
        long CRO = teamId("Croatia");
        long JPN = teamId("Japan");
        joinGroup(gA, ARG); joinGroup(gA, BRA); joinGroup(gA, CRO); joinGroup(gA, JPN);

        // Sinh đúng 6 trận vòng tròn 1 lượt
        schedule(gA, ARG, BRA);
        schedule(gA, ARG, CRO);
        schedule(gA, ARG, JPN);
        schedule(gA, BRA, CRO);
        schedule(gA, BRA, JPN);
        schedule(gA, CRO, JPN);

        // Chạy kết quả (bộ tỉ số ngắn gọn – có thắng, hòa, thua đủ để kiểm)
        playAndApply(gA, "Argentina", "Brazil",  2, 1); // ARG thắng
        playAndApply(gA, "Argentina", "Croatia", 1, 1); // Hòa
        playAndApply(gA, "Argentina", "Japan",   0, 1); // JPN thắng
        playAndApply(gA, "Brazil",    "Croatia", 3, 0); // BRA thắng
        playAndApply(gA, "Brazil",    "Japan",   2, 2); // Hòa
        playAndApply(gA, "Croatia",   "Japan",   1, 0); // CRO thắng

        Team tARG = teamDao.findByName("Argentina").orElseThrow();
        Team tBRA = teamDao.findByName("Brazil").orElseThrow();
        Team tCRO = teamDao.findByName("Croatia").orElseThrow();
        Team tJPN = teamDao.findByName("Japan").orElseThrow();

        assertEquals(4, tARG.getPoints());
        assertEquals(0, tARG.getGoalDifference());
        assertEquals(4, tBRA.getPoints());
        assertEquals(2, tBRA.getGoalDifference());
        assertEquals(4, tCRO.getPoints());
        assertEquals(-2, tCRO.getGoalDifference());
        assertEquals(4, tJPN.getPoints());
        assertEquals(0, tJPN.getGoalDifference());
        
        // Kỳ vọng điểm & hiệu số:
        //ARG: 4 điểm, GD 0
        //BRA: 4 điểm, GD +2
        //CRO: 4 điểm, GD -2
        //JPN: 4 điểm, GD 0
    }
}
