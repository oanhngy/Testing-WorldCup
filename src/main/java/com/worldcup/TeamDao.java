package com.worldcup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class TeamDao {
    //insert
    public long insert(Team t) throws SQLException {
        String sql = """
            insert into teams(name, region, coach, medical_staff, host, points, goal_diff)
            values (?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection c = SimpleDB.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getName());
            ps.setString(2, t.getRegion());
            ps.setString(3, t.getCoach());
            ps.setString(4, t.getMedicalStaff());
            ps.setInt(5, t.isHost() ? 1 : 0);
            ps.setInt(6, t.getPoints());
            ps.setInt(7, t.getGoalDifference());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        }
    }

    //find by id
    public Optional<Team> findById(long id) throws SQLException {
        String sql = "select * from teams where id=?";
        try (Connection c = SimpleDB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                Team t = new Team(
                        rs.getString("name"),
                        rs.getString("region"),
                        rs.getString("coach"),
                        rs.getString("medical_staff")
                );
                t.setId(rs.getLong("id"));
                t.setHost(rs.getInt("host") == 1);
                t.addPoints(rs.getInt("points"));
                int gd = rs.getInt("goal_diff");
                if (gd != 0) t.updateGoalDifference(gd, 0); 
                return Optional.of(t);
            }
        }
    }

    // find by name
    public Optional<Team> findByName(String name) throws SQLException {
        String sql = "select * from teams where name = ?";
        try (Connection c = SimpleDB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                Team t = new Team(
                        rs.getString("name"),
                        rs.getString("region"),
                        rs.getString("coach"),
                        rs.getString("medical_staff")
                );
                t.setId(rs.getLong("id"));
                t.setHost(rs.getInt("host") == 1);
                t.addPoints(rs.getInt("points"));
                int gd = rs.getInt("goal_diff");
                if (gd != 0) t.updateGoalDifference(gd, 0);
                return Optional.of(t);
            }
        }
    }


    public int deleteAll() throws SQLException {
        try (Connection c = SimpleDB.get(); Statement st = c.createStatement()) {
            return st.executeUpdate("delete from teams");
        }
    }
}
