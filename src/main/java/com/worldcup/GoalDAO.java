package com.worldcup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GoalDAO {
    //==== HELPERS ====
    //tìm team_id từ bảng team
    private long requireTeamId(Connection c, Team team) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "select id from teams where name=?")) {
            ps.setString(1, team.getName());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new SQLException("Khong tim thay team: " + team.getName());
    }

    //tìm player_id
    private Optional<Long> findPlayerId(Connection c, Player player, Team team) throws SQLException {
        String sql = """
            select p.id
            from players p
            join teams t on t.id = p.team_id
            where p.name = ? and t.name = ?
            limit 1
        """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, player.getName());
            ps.setString(2, team.getName());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(rs.getLong(1));
                return Optional.empty();
            }
        }
    }
    
    //tạo player từ player_id
    private Player buildPlayer(Connection c, long playerId) throws SQLException {
        String sql = """
            select p.name, p.shirt_no, p.position, t.name as team_name
            from players p
            join teams t on t.id = p.team_id
            where p.id = ?
        """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, playerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Khong tim thay player id=" + playerId);
                String name = rs.getString("name");
                int number = rs.getInt("shirt_no");
                String position = rs.getString("position");
                String teamName = rs.getString("team_name");
                return new Player(name, number, position, teamName);
            }
        }
    }

    //tạo team từ team_id
    private Team buildTeam(Connection c, long teamId) throws SQLException {
        String sql = "select name, region, coach, medical_staff from teams where id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Khong tim thay team id=" + teamId);
                return new Team(
                        rs.getString("name"),
                        rs.getString("region"),
                        rs.getString("coach"),
                        rs.getString("medical_staff")
                );
            }
        }
    }

    //tạo match cơ bản từ match_id
    private Match buildMatch(Connection c, long matchId) throws SQLException {
        String sql = "select team_a_id, team_b_id from matches where id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, matchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Khong tim thay match id=" + matchId);
                Team teamA = buildTeam(c, rs.getLong("team_a_id"));
                Team teamB = buildTeam(c, rs.getLong("team_b_id"));
                return new Match(teamA, teamB);
            }
        }
    }
    //==== HELPERS ====



    //HÀM CHÍNH
    //thêm 
    public long insert(long matchId, Goal goal) throws SQLException {
        String sql = """
            insert into goals(match_id, team_id, player_id, minute, own_goal)
            values (?, ?, ?, ?, 0)
        """;
        try (Connection c = SimpleDB.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            long teamId = requireTeamId(c, goal.getTeam());
            Long playerId = findPlayerId(c, goal.getPlayer(), goal.getTeam())
                    .orElseThrow(() -> new SQLException(
                            "Khong tim thay player '" + goal.getPlayer().getName() +
                            "' cua doi '" + goal.getTeam().getName() + "'"));

            ps.setLong(1, matchId);
            ps.setLong(2, teamId);
            ps.setLong(3, playerId);
            ps.setInt(4, goal.getMinute());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        }
    }

    //lấy toàn bộ danh sách Goal của 1 trận theo thứ tự
    public List<Goal> findByMatch(long matchId) throws SQLException {
        String sql = """
            select g.id, g.team_id, g.player_id, g.minute
            from goals g
            where g.match_id = ?
            order by g.minute asc, g.id asc
        """;
        try (Connection c = SimpleDB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, matchId);
            try (ResultSet rs = ps.executeQuery()) {
                Match match = buildMatch(c, matchId); 
                List<Goal> list = new ArrayList<>();
                while (rs.next()) {
                    Team team     = buildTeam(c, rs.getLong("team_id"));
                    Player player = buildPlayer(c, rs.getLong("player_id"));
                    int minute    = rs.getInt("minute");
                    list.add(new Goal(player, team, minute, match));
                }
                return list;
            }
        }
    }

    //xóa all goal trong 1 trận
    public int deleteAllByMatch(long matchId) throws SQLException {
        try (Connection c = SimpleDB.get();
             PreparedStatement ps = c.prepareStatement("delete from goals where match_id=?")) {
            ps.setLong(1, matchId);
            return ps.executeUpdate();
        }
    }
}
