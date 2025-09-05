package com.worldcup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MatchDAO {
    //tạo match
    public long insert(Long groupId, long teamAId, long teamBId, boolean isKnockout) throws SQLException {
        final String sql = """
            insert into matches(group_id, team_a_id, team_b_id, goals_a, goals_b, status, is_knockout)
            values (?, ?, ?, 0, 0, 'SCHEDULED', ?)
            """;
        try (Connection c = SimpleDB.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (groupId == null) ps.setNull(1, Types.INTEGER); else ps.setLong(1, groupId);
            ps.setLong(2, teamAId);
            ps.setLong(3, teamBId);
            ps.setInt(4, isKnockout ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new SQLException("Không tạo được match");
    }

    //đồng bộ tỉ số
    public int syncScoreFromGoals(long matchId) throws SQLException {
        final String q = """
            select m.team_a_id, m.team_b_id
            from matches m where m.id=?
            """;
        long teamAId;
        long teamBId;
        try (Connection c = SimpleDB.get();
             PreparedStatement ps = c.prepareStatement(q)) {
            ps.setLong(1, matchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return 0;
                teamAId = rs.getLong(1);
                teamBId = rs.getLong(2);
            }
        }

        final String countSql = "select count(*) from goals g where g.match_id=? and g.team_id=?";
        int ga, gb;
        try (Connection c = SimpleDB.get();
             PreparedStatement c1 = c.prepareStatement(countSql);
             PreparedStatement c2 = c.prepareStatement(countSql)) {
            c1.setLong(1, matchId); c1.setLong(2, teamAId);
            try (ResultSet rs = c1.executeQuery()) { rs.next(); ga = rs.getInt(1); }
            c2.setLong(1, matchId); c2.setLong(2, teamBId);
            try (ResultSet rs = c2.executeQuery()) { rs.next(); gb = rs.getInt(1); }
        }

        final String upd = "update matches set goals_a=?, goals_b=? where id=?";
        try (Connection c = SimpleDB.get();
             PreparedStatement ps = c.prepareStatement(upd)) {
            ps.setInt(1, ga);
            ps.setInt(2, gb);
            ps.setLong(3, matchId);
            return ps.executeUpdate();
        }
    }

    //cho finish 1 trận: cập nhật tỉ số, status FINISHED
    public int finish(long matchId, int goalsA, int goalsB) throws SQLException {
        final String sql = """
           update matches
              set goals_a=?, goals_b=?, status='FINISHED'
            where id=?
           """;
        try (Connection c = SimpleDB.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, goalsA);
            ps.setInt(2, goalsB);
            ps.setLong(3, matchId);
            return ps.executeUpdate();
        }
    }

    //lấy tỉ số hiện tại
    public Optional<int[]> getScore(long matchId) throws SQLException {
        final String sql = "select goals_a, goals_b from matches where id=?";
        try (Connection c = SimpleDB.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, matchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(new int[]{rs.getInt(1), rs.getInt(2)});
                return Optional.empty();
            }
        }
    }

    //lấy status hiện tại, scheduled/ finished
    public Optional<String> getStatus(long matchId) throws SQLException {
        try (Connection c = SimpleDB.get();
             PreparedStatement ps = c.prepareStatement("select status from matches where id=?")) {
            ps.setLong(1, matchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(rs.getString(1));
                return Optional.empty();
            }
        }
    }

    //liệt kê match_id thuộc 1 group theo id tăng
    public List<Long> listByGroup(long groupId) throws SQLException {
        final String sql = "select id from matches where group_id=? order by id asc";
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

    //liệt kê match_id thuộc knock out stage, id tăng dần
    public List<Long> listKnockout() throws SQLException {
        final String sql = "select id from matches where is_knockout=1 order by id asc";
        List<Long> out = new ArrayList<>();
        try (Connection c = SimpleDB.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(rs.getLong(1));
        }
        return out;
    }

    //xóa trận theo id( goal bị xóa theo do FK)
    public int deleteById(long matchId) throws SQLException {
        try (Connection c = SimpleDB.get();
             PreparedStatement ps = c.prepareStatement("delete from matches where id=?")) {
            ps.setLong(1, matchId);
            return ps.executeUpdate();
        }
    }

    //xóa all trận trong 1 group (goal xóa luọn)
    public int deleteAllByGroup(long groupId) throws SQLException {
        try (Connection c = SimpleDB.get();
             PreparedStatement ps = c.prepareStatement("delete from matches where group_id=?")) {
            ps.setLong(1, groupId);
            return ps.executeUpdate();
        }
    }
}
