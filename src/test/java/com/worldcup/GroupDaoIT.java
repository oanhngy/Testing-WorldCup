package com.worldcup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GroupDaoIT extends BaseIT {
    private GroupDAO gdao;

    @BeforeEach
    void setup() {
        gdao=new GroupDAO();
    }

    //==== HELPERS ====
    //thêm team
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
    //==== HELPERS ====


    //tạo gr, trả đủ 4 id
    @Test
    void createGroup_addFourTeams_listTeamIds_shouldReturn4() throws Exception {
        long gid = gdao.insert("A");
        assertTrue(gid > 0);

        long t1 = insertTeam("Argentina", "SA");
        long t2 = insertTeam("France", "EU");
        long t3 = insertTeam("Brazil", "SA");
        long t4 = insertTeam("Germany", "EU");

        assertEquals(1, gdao.addTeam(gid, t1));
        assertEquals(1, gdao.addTeam(gid, t2));
        assertEquals(1, gdao.addTeam(gid, t3));
        assertEquals(1, gdao.addTeam(gid, t4));

        List<Long> ids = gdao.listTeamIds(gid);
        assertEquals(4, ids.size());
        assertTrue(ids.containsAll(List.of(t1, t2, t3, t4)));
    }
}
