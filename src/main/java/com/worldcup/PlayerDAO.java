package com.worldcup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlayerDAO {
    private long requireTeamId(Connection c, String teamName) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("select id from teams where name=?")) {
            ps.setString(1, teamName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new SQLException("Khong tim thay team: " + teamName);
    }

    private int countGoalsByPlayerId(Connection c, long playerId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "select count(*) from goals where player_id=?")) {
            ps.setLong(1, playerId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private Player mapRow(Connection c, ResultSet rs, String teamName) throws SQLException {
        String name     = rs.getString("name");
        int number      = rs.getInt("shirt_no");
        String position = rs.getString("position");
        Player p = new Player(name, number, position, teamName);

        //thẻ vàng, đỏ
        int yc = rs.getInt("yellows");
        int rc = rs.getInt("reds");
        for (int i = 0; i < yc; i++) p.addYellowCard();
        for (int i = 0; i < rc; i++) p.addRedCard();

        //lấy bàn thắng từ bảng goals theo player_id
        long playerId = rs.getLong("id");
        int goals = countGoalsByPlayerId(c, playerId);
        for (int i = 0; i < goals; i++) p.scoreGoal();

        return p;
    }

    //HÀM CHÍNH
    //add
    public long insert(Player player) throws SQLException {
        String sql = """
            insert into players(team_id, name, shirt_no, position, yellows, reds)
            values (?, ?, ?, ?, ?, ?)
        """;
        try (Connection c = SimpleDB.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            long teamId = requireTeamId(c, player.getTeamName());
            ps.setLong(1, teamId);
            ps.setString(2, player.getName());
            ps.setInt(3, player.getNumber());
            ps.setString(4, player.getPosition());
            ps.setInt(5, player.getYellowCards());
            ps.setInt(6, player.getRedCards());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        }
    }

    //find by id
    public Optional<Player> findById(long id) throws SQLException {
        String sql = """
            select p.*, t.name as team_name
            from players p
            join teams t on t.id = p.team_id
            where p.id = ?
        """;
        try (Connection c = SimpleDB.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                String teamName = rs.getString("team_name");
                Player p = mapRow(c, rs, teamName);
                return Optional.of(p);
            }
        }
    }

    //xuất dsach cầu thủ = teamName
    public List<Player> findByTeamName(String teamName) throws SQLException {
        String sql = """
            select p.*
            from players p
            join teams t on t.id = p.team_id
            where t.name = ?
            order by p.shirt_no, p.name
        """;
        try (Connection c = SimpleDB.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, teamName);
            try (ResultSet rs = ps.executeQuery()) {
                List<Player> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(c, rs, teamName));
                }
                return list;
            }
        }
    }

    //update thẻ(thủ công)
    public int updateCards(long playerId, int yellowCards, int redCards) throws SQLException {
        try (Connection c = SimpleDB.get();
             PreparedStatement ps = c.prepareStatement(
                     "update players set yellows=?, reds=? where id=?")) {
            ps.setInt(1, yellowCards);
            ps.setInt(2, redCards);
            ps.setLong(3, playerId);
            return ps.executeUpdate();
        }
    }

    //xóa hết cầu thủ= teamName
    public int deleteAllByTeamName(String teamName) throws SQLException {
        String sql = """
            delete from players
            where team_id = (select id from teams where name = ?)
        """;
        try (Connection c = SimpleDB.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, teamName);
            return ps.executeUpdate();
        }
    }
}
