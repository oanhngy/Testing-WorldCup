package com.worldcup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GoalDaoIT extends BaseIT {
    private GoalDAO gdao;
    private PlayerDAO pdao;

    @BeforeEach
    void setpup() {
        gdao=new GoalDAO();
        pdao=new PlayerDAO();
    }

    //==== HELPERS ====
    //trả về team_id theo name
    private long requireTeamId(String name) throws SQLException {
        try (Connection c = SimpleDB.get();
             PreparedStatement ps = c.prepareStatement("select id from teams where name=?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new IllegalStateException("Khong tim thay team: " + name);
    }

    //tạo 1 match, trả về match_id
    private long insertMatch(String teamAName, String teamBName) throws SQLException {
        long teamAId = requireTeamId(teamAName);
        long teamBId = requireTeamId(teamBName);
        String sql = """
            insert into matches(group_id, team_a_id, team_b_id, goals_a, goals_b, status, is_knockout)
            values (NULL, ?, ?, 0, 0, 'SCHEDULED', 0)
        """;
        try (Connection c = SimpleDB.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, teamAId);
            ps.setLong(2, teamBId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new IllegalStateException("Khong tao duoc match " + teamAName + " vs " + teamBName+ "!");
    }

    //tạo Match obj cơ bản
    private Match makeMatch(Team a, Team b) {
        return new Match(a, b);
    }
    //==== HELPERS ====

    //chèn bàn thắng cho trận A vs B, player đã tồn taii5
    @Test
    void insertOneGoal_thenFindByMatch_shouldReturnSameData() throws Exception {
        //tạo 2 đội & 1 trận
        teamDao.insert(new Team("Argentina", "SA", "Scaloni", "MedA"));
        teamDao.insert(new Team("France", "EU", "Deschamps", "MedB"));
        long matchId = insertMatch("Argentina", "France");

        long pid = pdao.insert(new Player("Lionel Messi", 10, "FW", "Argentina"));
        assertTrue(pid > 0);

        Team arg = new Team("Argentina", "SA", "Scaloni", "MedA");
        Team fra = new Team("France", "EU", "Deschamps", "MedB");
        Match m = makeMatch(arg, fra);
        Goal g = new Goal(new Player("Lionel Messi", 10, "FW", "Argentina"), arg, 23, m);

        long gid = gdao.insert(matchId, g);
        assertTrue(gid > 0, "insert goal phai tra ve id > 0");

        List<Goal> got = gdao.findByMatch(matchId);

        assertEquals(1, got.size());
        Goal one = got.get(0);
        assertEquals("Lionel Messi", one.getPlayer().getName());
        assertEquals("Argentina", one.getTeam().getName());
        assertEquals(23, one.getMinute());
        assertEquals("Argentina", one.getMatch().getTeamA().getName());
        assertEquals("France", one.getMatch().getTeamB().getName());
    }

    //chèn nhiều goal trong 1 trận
    @Test
    void insertManyGoals_sameMatch_thenFindByMatch_shouldBeSortedByMinute() throws Exception {
        teamDao.insert(new Team("Brazil", "SA", "CoachA", "MedA"));
        teamDao.insert(new Team("Germany", "EU", "CoachB", "MedB"));
        long matchId = insertMatch("Brazil", "Germany");

        pdao.insert(new Player("Neymar", 10, "FW", "Brazil"));
        pdao.insert(new Player("Vini Jr", 20, "FW", "Brazil"));
        pdao.insert(new Player("Havertz", 7, "FW", "Germany"));

        Team bra = new Team("Brazil", "SA", "CoachA", "MedA");
        Team ger = new Team("Germany", "EU", "CoachB", "MedB");
        Match m = makeMatch(bra, ger);

        Goal g1 = new Goal(new Player("Neymar", 10, "FW", "Brazil"), bra, 7, m);
        Goal g2 = new Goal(new Player("Havertz", 7, "FW", "Germany"), ger, 15, m);
        Goal g3 = new Goal(new Player("Vini Jr", 20, "FW", "Brazil"), bra, 69, m);

        gdao.insert(matchId, g2);
        gdao.insert(matchId, g3);
        gdao.insert(matchId, g1);

        List<Goal> list = gdao.findByMatch(matchId);

        assertEquals(3, list.size());
        assertEquals("Neymar",  list.get(0).getPlayer().getName());
        assertEquals("Havertz", list.get(1).getPlayer().getName());
        assertEquals("Vini Jr", list.get(2).getPlayer().getName());
    }

    //xóa all goals trong match
    @Test
    void deleteAllByMatch_shouldRemoveAllGoalsOfThatMatch() throws Exception {
        teamDao.insert(new Team("Spain", "EU", "DLF", "Med"));
        teamDao.insert(new Team("Italy", "EU", "Spalletti", "Med"));
        long matchId = insertMatch("Spain", "Italy");

        pdao.insert(new Player("Morata", 9, "FW", "Spain"));
        pdao.insert(new Player("Yamal", 19, "FW", "Spain"));

        Team sp = new Team("Spain", "EU", "DLF", "Med");
        Team it = new Team("Italy", "EU", "Spalletti", "Med");
        Match m = makeMatch(sp, it);

        gdao.insert(matchId, new Goal(new Player("Morata", 9, "FW", "Spain"), sp, 12, m));
        gdao.insert(matchId, new Goal(new Player("Yamal", 19, "FW", "Spain"),  sp, 44, m));
        assertEquals(2, gdao.findByMatch(matchId).size());

        int deleted = gdao.deleteAllByMatch(matchId);

        assertTrue(deleted >= 2);
        assertTrue(gdao.findByMatch(matchId).isEmpty());
    }

    //check ném exception nếu player k có trong db
    @Test
    void insertGoal_withUnknownPlayer_shouldThrowSQLException() throws Exception {
        teamDao.insert(new Team("Portugal", "EU", "Martinez", "Med"));
        teamDao.insert(new Team("Netherlands", "EU", "Koeman", "Med"));
        long matchId = insertMatch("Portugal", "Netherlands");

        Team por = new Team("Portugal", "EU", "Martinez", "Med");
        Team ned = new Team("Netherlands", "EU", "Koeman", "Med");
        Match m = makeMatch(por, ned);
        Goal g = new Goal(new Player("Cristiano Ronaldo", 7, "FW", "Portugal"), por, 5, m);

        assertThrows(SQLException.class, () -> gdao.insert(matchId, g));
    }
}

