package com.worldcup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class StandingService {
    //xếp hạng 1 đội trong bảng
    public static class Row {
        public final long teamId;
        public final String teamName;
        public final int points;
        public final int goalDiff;
        public final int cardPts; // yellows+ 2 reds

        public Row(long teamId, String teamName, int points, int goalDiff, int cardPts) {
            this.teamId = teamId;
            this.teamName = teamName;
            this.points = points;
            this.goalDiff = goalDiff;
            this.cardPts = cardPts;
        }

        @Override public String toString() {
            return String.format("%-14s  Pts=%2d  GD=%+2d  Cards=%2d", teamName, points, goalDiff, cardPts);
        }        
    }

    //lấy bxh cho group
    public List<Row> getGroupTable(long groupId) throws SQLException {
        List<Row> rows = loadRows(groupId);

        // comparator theo ưu tiên
        rows.sort((a, b) -> {
            //điểm
            if (a.points != b.points) return Integer.compare(b.points, a.points);
            //hiệu số
            if (a.goalDiff != b.goalDiff) return Integer.compare(b.goalDiff, a.goalDiff);
            //thẻ
            if (a.cardPts != b.cardPts) return Integer.compare(a.cardPts, b.cardPts);
            //đối đầu
            try {
                int h2h = headToHeadPointDelta(groupId, a.teamId, b.teamId);
                if (h2h != 0) return -Integer.compare(0, h2h);
            } catch (SQLException ignored) {}

            int ra = seededRank(groupId, a.teamId);
            int rb = seededRank(groupId, b.teamId);
            return Integer.compare(ra, rb);
        });

        return rows;
    }

    //lấy top 2/bảng
    public List<Long> top2EachGroup() throws SQLException {
        List<Long> top = new ArrayList<>(16);
        Map<Long, String> groups = loadGroupsOrdered();
        for (Map.Entry<Long, String> e : groups.entrySet()) {
            long gid = e.getKey();
            List<Row> table = getGroupTable(gid);
            if (table.size() >= 2) {
                top.add(table.get(0).teamId);
                top.add(table.get(1).teamId);
            }
        }
        return top;
    }

    //==== HELPERS ====
    //load 4 đội
    private List<Row> loadRows(long groupId) throws SQLException {
        final String sql = """
            select t.id, t.name, t.points, t.goal_diff,
                   coalesce(p.yc, 0) + 2*coalesce(p.rc, 0) as cardPts
              from group_teams gt
              join teams t on t.id = gt.team_id
              left join (
                 select team_id, sum(yellows) as yc, sum(reds) as rc
                   from players
                  group by team_id
              ) p on p.team_id = t.id
             where gt.group_id = ?
             order by t.name asc
            """;
        List<Row> rows = new ArrayList<>(4);
        try (Connection c = SimpleDB.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Row(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getInt(3),
                        rs.getInt(4),
                        rs.getInt(5)
                    ));
                }
            }
        }
        return rows;
    }

    //tính A-points, B-points
    private int headToHeadPointDelta(long groupId, long teamAId, long teamBId) throws SQLException {
        final String sql = """
            select goals_a, goals_b, team_a_id, team_b_id
              from matches
             where group_id = ?
               and status = 'FINISHED'
               and ((team_a_id = ? and team_b_id = ?) or (team_a_id = ? and team_b_id = ?))
            """;
        int ptsA = 0, ptsB = 0;
        try (Connection c = SimpleDB.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, groupId);
            ps.setLong(2, teamAId); ps.setLong(3, teamBId);
            ps.setLong(4, teamBId); ps.setLong(5, teamAId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int ga = rs.getInt(1), gb = rs.getInt(2);
                    long ta = rs.getLong(3), tb = rs.getLong(4);
                    // chuẩn hóa: điểm về phía teamA
                    if (ta == teamAId) {
                        if (ga > gb) ptsA += 3;
                        else if (ga == gb) { ptsA += 1; ptsB += 1; }
                        else ptsB += 3;
                    } else {
                        if (gb > ga) ptsA += 3; // A là team_b và thắng
                        else if (ga == gb) { ptsA += 1; ptsB += 1; }
                        else ptsB += 3;
                    }
                }
            }
        }
        return ptsA - ptsB;
    }

    //random tie-break
    private int seededRank(long groupId, long teamId) {
        long seed = 1469598103934665603L ^ (groupId * 1099511628211L) ^ teamId;
        Random r = new Random(seed);
        return r.nextInt(1_000_000);
    }

    //map group
    private Map<Long, String> loadGroupsOrdered() throws SQLException {
        final String sql = "select id, name from groups order by name asc";
        Map<Long, String> out = new LinkedHashMap<>();
        try (Connection c = SimpleDB.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.put(rs.getLong(1), rs.getString(2));
        }
        return out;
    }
}
