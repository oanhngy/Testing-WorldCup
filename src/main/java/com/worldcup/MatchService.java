package com.worldcup;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MatchService {
    private final MatchDAO matchDao=new MatchDAO();
    private final GoalDAO goalDao=new GoalDAO();
    private final PlayerDAO playerDao=new PlayerDAO();
    private final Random rnd=new Random();

    //==== HELPERS ====
    //lấy team id 2 đội+ tên cho 1 matchId
    private TeamIdsAndNames loadTeamsOfMatch(long matchId) throws SQLException {
        final String sql = """
            select m.team_a_id, ta.name, ta.region,
                   m.team_b_id, tb.name, tb.region
              from matches m
              join teams ta on ta.id = m.team_a_id
              join teams tb on tb.id = m.team_b_id
             where m.id = ?
            """;
        try (Connection c = SimpleDB.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, matchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Khong tim thay match id=" + matchId);
                }
                long teamAId    = rs.getLong(1);
                String teamAName= rs.getString(2);
                String teamAReg = rs.getString(3);
                long teamBId    = rs.getLong(4);
                String teamBName= rs.getString(5);
                String teamBReg = rs.getString(6);
                return new TeamIdsAndNames(teamAId, teamAName, teamAReg, teamBId, teamBName, teamBReg);
            }
        }
    }

    //cộng dồn điểm, hiệu số trong 1 đội theo id
    private void applyTeamDelta(long teamId, int pointsDelta, int goalDiffDelta) throws SQLException {
        final String sql = "update teams set points = points + ?, goal_diff = goal_diff + ? where id = ?";
        try (Connection c = SimpleDB.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, pointsDelta);
            ps.setInt(2, goalDiffDelta);
            ps.setLong(3, teamId);
            ps.executeUpdate();
        }
    }

    //tạo object Match
    private Match buildDomainMatch(String teamAName, String teamARegion,
                                   String teamBName, String teamBRegion) {
        Team ta = new Team(teamAName, teamARegion, "", "");
        Team tb = new Team(teamBName, teamBRegion, "", "");
        return new Match(ta, tb);
    }

    //random bàn thắng
    private void addRandomGoals(List<Goal> out, int count,
                                String teamName, String teamRegion,
                                List<Player> squad, Match m) {
        if (count <= 0) return;
        if (squad == null || squad.isEmpty()) return; // nếu chưa có cầu thủ, bỏ qua tránh lỗi FK

        Team team = new Team(teamName, teamRegion, "", "");
        for (int i = 0; i < count; i++) {
            Player p = squad.get(rnd.nextInt(squad.size()));
            int minute = 1 + rnd.nextInt(90);
            out.add(new Goal(
                    new Player(p.getName(), p.getNumber(), p.getPosition(), teamName),
                    team,
                    minute,
                    m
            ));
        }
    }
    //==== HELPERS ====



    //chèn dsach bàn thắng, update tỉ số, end trận
    public void recordResult(long matchId, int goalsA, int goalsB, List<Goal> goals) throws SQLException {
        //lấy id, tên đội
        TeamIdsAndNames t = loadTeamsOfMatch(matchId);
        //chèn goals
        if (goals != null) {
            for (Goal g : goals) {
                goalDao.insert(matchId, g);
            }
        }
        //chốt tỉ số
        matchDao.finish(matchId, goalsA, goalsB);

        //update điểm, hiệu số
        int gdA = goalsA - goalsB;
        int gdB = goalsB - goalsA;
        int ptsA = (goalsA > goalsB) ? 3 : (goalsA == goalsB ? 1 : 0);
        int ptsB = (goalsB > goalsA) ? 3 : (goalsA == goalsB ? 1 : 0);
        applyTeamDelta(t.teamAId, ptsA, gdA);
        applyTeamDelta(t.teamBId, ptsB, gdB);
    }

    //simulate trận đấu(random tỉ số, bàn thắng)
    public int[] simulateGroupMatch(long matchId) throws SQLException {
        TeamIdsAndNames t = loadTeamsOfMatch(matchId);

        List<Player> playersA = playerDao.findByTeamName(t.teamAName);
        List<Player> playersB = playerDao.findByTeamName(t.teamBName);

        int goalsA = rnd.nextInt(5);
        int goalsB = rnd.nextInt(5);

        List<Goal> goals = new ArrayList<>();
        Match domainMatch = buildDomainMatch(t.teamAName, t.teamARegion, t.teamBName, t.teamBRegion);

        addRandomGoals(goals, goalsA, t.teamAName, t.teamARegion, playersA, domainMatch);
        addRandomGoals(goals, goalsB, t.teamBName, t.teamBRegion, playersB, domainMatch);

        recordResult(matchId, goalsA, goalsB, goals);
        return new int[]{goalsA, goalsB};
    }

    private static class TeamIdsAndNames {
        final long teamAId;  final String teamAName;  final String teamARegion;
        final long teamBId;  final String teamBName;  final String teamBRegion;
        TeamIdsAndNames(long aId, String aName, String aRegion,
                        long bId, String bName, String bRegion) {
            this.teamAId = aId; this.teamAName = aName; this.teamARegion = aRegion;
            this.teamBId = bId; this.teamBName = bName; this.teamBRegion = bRegion;
        }
    }
}
