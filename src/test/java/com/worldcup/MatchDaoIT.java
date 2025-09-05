package com.worldcup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MatchDaoIT extends BaseIT {
    private MatchDAO mdao;
    private GoalDAO gdao;

    @BeforeEach
    void setup() {
        mdao=new MatchDAO();
        gdao=new GoalDAO();
    }

    //==== HELPERS ====
    //tạo group trả về id
    private long insertGroup(String code) throws SQLException {
        try (Connection c = SimpleDB.get();
             PreparedStatement ps = c.prepareStatement("insert into groups(name) values(?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new IllegalStateException("Khong tao duoc group " + code);
    }

    //thêm team trả về id
    private long insertTeam(String name, String region) throws SQLException {
        final String sql = """
            insert into teams(name, region, coach, medical_staff, host, points, goal_diff)
            values(?, ?, '', '', 0, 0, 0)
            """;
        try (Connection c = SimpleDB.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, region);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new IllegalStateException("Không tạo được team " + name);
    }

    //đếm goal trong 1 match
    private int countGoals(long matchId) throws SQLException {
        try (Connection c = SimpleDB.get();
             PreparedStatement ps = c.prepareStatement("select count(*) from goals where match_id=?")) {
            ps.setLong(1, matchId);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        }
    }
    //==== HELPERS ====



    //tạo match vòng bảng: check id, status, tỉ số 0-0 default
    @Test
    void insert_groupMatch_shouldBeScheduledWithZeroScore() throws Exception {
        long gid = insertGroup("A");
        long ta  = insertTeam("Argentina", "SA");
        long tb  = insertTeam("France", "EU");

        long mid = mdao.insert(gid, ta, tb, false);
        assertTrue(mid > 0);
        assertEquals("SCHEDULED", mdao.getStatus(mid).orElse("NA"));
        int[] score = mdao.getScore(mid).orElseThrow();
        assertArrayEquals(new int[]{0,0}, score);
    }

    //tạo match knock-out: group_id null, is_knockout=1
    @Test
    void insert_knockoutMatch_shouldAppearInKnockoutList() throws Exception {
        long ta  = insertTeam("Brazil", "SA");
        long tb  = insertTeam("Germany", "EU");

        long mid = mdao.insert(null, ta, tb, true);
        assertTrue(mid > 0);

        List<Long> kos = mdao.listKnockout();
        assertTrue(kos.contains(mid));
    }

    //chèn goals, đồng bộ= syncScoreFromGoal, có finish, check tỉ số, status
    @Test
    void syncScoreFromGoals_thenFinish_shouldReflectGoalsAndFinish() throws Exception {
        long gid = insertGroup("B");
        long ta  = insertTeam("Spain", "EU");
        long tb  = insertTeam("Italy", "EU");

        long mid = mdao.insert(gid, ta, tb, false);

        // tạo các object domain theo model của bạn đã dùng trong GoalDaoIT
        Team sp = new Team("Spain", "EU", "", "");
        Team it = new Team("Italy", "EU", "", "");
        Match m = new Match(sp, it);

        //cần có player trước khi insert goal
        PlayerDAO pdao = new PlayerDAO();
        pdao.insert(new Player("Morata", 9, "FW", "Spain"));
        pdao.insert(new Player("Yamal", 19, "FW", "Spain"));
        pdao.insert(new Player("Chiesa", 14, "FW", "Italy"));

        gdao.insert(mid, new Goal(new Player("Morata", 9, "FW", "Spain"), sp, 12, m)); // Spain 1-0
        gdao.insert(mid, new Goal(new Player("Chiesa", 14, "FW", "Italy"), it, 33, m)); // 1-1
        gdao.insert(mid, new Goal(new Player("Yamal", 19, "FW", "Spain"), sp, 70, m)); // 2-1

        int upd = mdao.syncScoreFromGoals(mid);
        assertEquals(1, upd);

        int[] score = mdao.getScore(mid).orElseThrow();
        assertArrayEquals(new int[]{2,1}, score);

        int fin = mdao.finish(mid, score[0], score[1]);
        assertEquals(1, fin);
        assertEquals("FINISHED", mdao.getStatus(mid).orElse("NA"));
    }

    //trả về đúng các match vừa tạo theo id tăng
    @Test
    void listByGroup_shouldReturnAscendingIds() throws Exception {
        long gid = insertGroup("C");
        long t1 = insertTeam("A", "EU");
        long t2 = insertTeam("B", "EU");
        long t3 = insertTeam("C", "EU");

        long m1 = mdao.insert(gid, t1, t2, false);
        long m2 = mdao.insert(gid, t1, t3, false);
        long m3 = mdao.insert(gid, t2, t3, false);

        assertEquals(List.of(m1, m2, m3), mdao.listByGroup(gid));
    }

    //xóa 1 trận có goal, check goal có xóa k
    @Test
    void deleteById_shouldCascadeDeleteGoals() throws Exception {
        long gid = insertGroup("D");
        long ta  = insertTeam("Portugal", "EU");
        long tb  = insertTeam("Netherlands", "EU");

        long mid = mdao.insert(gid, ta, tb, false);

        PlayerDAO pdao = new PlayerDAO();
        pdao.insert(new Player("Ronaldo", 7, "FW", "Portugal"));
        Team por = new Team("Portugal", "EU", "", "");
        Team ned = new Team("Netherlands", "EU", "", "");
        Match m  = new Match(por, ned);
        gdao.insert(mid, new Goal(new Player("Ronaldo", 7, "FW", "Portugal"), por, 5, m));
        assertEquals(1, countGoals(mid));

        assertEquals(1, mdao.deleteById(mid));
        assertEquals(0, countGoals(mid)); // goals đã xóa
    }

    //xóa all trận torng group, dsach cần rỗng
    @Test
    void deleteAllByGroup_shouldRemoveAllMatchesOfGroup() throws Exception {
        long gid = insertGroup("E");
        long t1 = insertTeam("X", "EU");
        long t2 = insertTeam("Y", "EU");

        mdao.insert(gid, t1, t2, false);
        mdao.insert(gid, t1, t2, false);

        int del = mdao.deleteAllByGroup(gid);
        assertTrue(del >= 2);
        assertTrue(mdao.listByGroup(gid).isEmpty());
    }
}
