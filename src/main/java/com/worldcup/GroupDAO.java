package com.worldcup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class GroupDAO {
    //tạo mới, trả về id
    public long insert(String name) throws SQLException {
        try (Connection c = SimpleDB.get();
             PreparedStatement ps = c.prepareStatement(
                     "insert into groups(name) values(?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new SQLException("Khong tao duoc group: " + name);
    }

    //thêm team mới vào group( lưu vào group_teams), trả về số bàn ghi thêm
    public int addTeam(long groupId, long teamId) throws SQLException {
        try (Connection c = SimpleDB.get();
             PreparedStatement ps = c.prepareStatement(
                     "insert into group_teams(group_id, team_id) values(?, ?)")) {
            ps.setLong(1, groupId);
            ps.setLong(2, teamId);
            return ps.executeUpdate();
        }
    }

    //liệt kê dsach team_id torng 1 group, id tăng dần
    public List<Long> listTeamIds(long groupId) throws SQLException {
        final String sql = """
            select gt.team_id
              from group_teams gt
             where gt.group_id=?
             order by gt.team_id asc
            """;
        List<Long> out = new ArrayList<>();
        try (Connection c = SimpleDB.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getLong(1));
            }
        }
        return out;
    }
}
