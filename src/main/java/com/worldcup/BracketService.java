package com.worldcup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BracketService {
    private final MatchDAO matchDao = new MatchDAO();
    private final GoalDAO goalDao   = new GoalDAO();
    private final PlayerDAO playerDao = new PlayerDAO();
    private final MatchService matchService = new MatchService();
    private final Random rnd = new Random();

    //8 cặp đấu
    public List<Long> createRoundOf16(Map<String, long[]> top2ByGroup) throws SQLException {
        //lấy cặp A-B, C-D, E-F, G-H
        List<Long> r16 = new ArrayList<>(8);
        r16.add(createKo(top2ByGroup.get("A")[0], top2ByGroup.get("B")[1]));
        r16.add(createKo(top2ByGroup.get("B")[0], top2ByGroup.get("A")[1]));
        r16.add(createKo(top2ByGroup.get("C")[0], top2ByGroup.get("D")[1]));
        r16.add(createKo(top2ByGroup.get("D")[0], top2ByGroup.get("C")[1]));
        r16.add(createKo(top2ByGroup.get("E")[0], top2ByGroup.get("F")[1])); 
        r16.add(createKo(top2ByGroup.get("F")[0], top2ByGroup.get("E")[1])); 
        r16.add(createKo(top2ByGroup.get("G")[0], top2ByGroup.get("H")[1])); 
        r16.add(createKo(top2ByGroup.get("H")[0], top2ByGroup.get("G")[1]));
        return r16;
    }

    //mô phỏng trận knock-out
    public int[] simulateKnockoutMatch(long matchId) throws SQLException {
        int a = rnd.nextInt(5);
        int b = rnd.nextInt(5);
        if (a == b) {
            //thêm random bàn thắng để k hòa
            if (rnd.nextBoolean()) a++; else b++;
        }

        TeamInfo t = loadTeams(matchId);
        List<Player> squadA = playerDao.findByTeamName(t.teamAName);
        List<Player> squadB = playerDao.findByTeamName(t.teamBName);
        Match m = new Match(new Team(t.teamAName, t.teamARegion, "", ""),
                            new Team(t.teamBName, t.teamBRegion, "", ""));
        List<Goal> goals = new ArrayList<>();
        addRandomGoals(goals, a, squadA, t.teamAName, t.teamARegion, m);
        addRandomGoals(goals, b, squadB, t.teamBName, t.teamBRegion, m);

        matchService.recordResult(matchId, a, b, goals);
        return new int[]{a, b};
    }

    //chạy tất cả trận knockout R16-final
    public Podium runElimination(Map<String, long[]> top2ByGroup) throws SQLException {
        //R16
        List<Long> r16 = createRoundOf16(top2ByGroup);
        List<Long> winR16 = simulateAndCollectWinners(r16);

        //R8
        List<Long> qf = createNextRound(winR16);
        List<Long> winQF = simulateAndCollectWinners(qf);

        //semi-final
        List<Long> sf = createNextRound(winQF);
        List<Long> winSF = new ArrayList<>(2);
        List<Long> loseSF = new ArrayList<>(2);
        for (Long mid : sf) {
            simulateKnockoutMatch(mid);
            long w = winnerOf(mid);
            long l = loserOf(mid);
            winSF.add(w); loseSF.add(l);
        }

        List<Long> p3 = new ArrayList<>(1);
        p3.add(createKo(loseSF.get(0), loseSF.get(1)));
        simulateKnockoutMatch(p3.get(0));
        long third1 = winnerOf(p3.get(0));
        long third2 = loserOf(p3.get(0)); //đồng hạng 3

        //final
        List<Long> fin = new ArrayList<>(1);
        fin.add(createKo(winSF.get(0), winSF.get(1)));
        simulateKnockoutMatch(fin.get(0));
        long champion = winnerOf(fin.get(0));
        long runnerUp = loserOf(fin.get(0));

        return new Podium(champion, runnerUp, third1, third2);
    }



    //==== HELPERS ====
    //tạo trận knock-out
    private long createKo(long teamAId, long teamBId) throws SQLException {
        return matchDao.insert(null, teamAId, teamBId, true);
    }

    //tạo trận kế tiếp
    private List<Long> createNextRound(List<Long> winners) throws SQLException {
        List<Long> next = new ArrayList<>(winners.size() / 2);
        for (int i = 0; i < winners.size(); i += 2) {
            next.add(createKo(winners.get(i), winners.get(i + 1)));
        }
        return next;
    }

    //mô phỏng all trận, trả winner
    private List<Long> simulateAndCollectWinners(List<Long> matchIds) throws SQLException {
        List<Long> winners = new ArrayList<>(matchIds.size());
        for (Long mid : matchIds) {
            simulateKnockoutMatch(mid);
            winners.add(winnerOf(mid));
        }
        return winners;
    }

    //team thắng trong 1 trận
    private long winnerOf(long matchId) throws SQLException {
        final String sql = "select team_a_id, team_b_id, goals_a, goals_b from matches where id=?";
        try (Connection c = SimpleDB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, matchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Khong tim thay match id=" + matchId);
                long ta = rs.getLong(1), tb = rs.getLong(2);
                int ga = rs.getInt(3), gb = rs.getInt(4);
                return (ga > gb) ? ta : tb;
            }
        }
    }

    //team thua trong 1 trận
    private long loserOf(long matchId) throws SQLException {
        final String sql = "select team_a_id, team_b_id, goals_a, goals_b from matches where id=?";
        try (Connection c = SimpleDB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, matchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Khong tim thay match id=" + matchId);
                long ta = rs.getLong(1), tb = rs.getLong(2);
                int ga = rs.getInt(3), gb = rs.getInt(4);
                return (ga > gb) ? tb : ta;
            }
        }
    }

    //tên, region 2 đội trong 1 match
    private TeamInfo loadTeams(long matchId) throws SQLException {
        final String sql = """
            select ta.name, ta.region, tb.name, tb.region
              from matches m
              join teams ta on ta.id = m.team_a_id
              join teams tb on tb.id = m.team_b_id
             where m.id = ?
            """;
        try (Connection c = SimpleDB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, matchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Khong tim thay match id=" + matchId);
                return new TeamInfo(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4));
            }
        }
    }

    //random thêm bàn
    private void addRandomGoals(List<Goal> out, int count,
                                List<Player> squad, String teamName, String region, Match m) {
        if (count <= 0 || squad == null || squad.isEmpty()) return;
        Team team = new Team(teamName, region, "", "");
        for (int i = 0; i < count; i++) {
            Player p = squad.get(rnd.nextInt(squad.size()));
            int minute = 1 + rnd.nextInt(90);
            out.add(new Goal(new Player(p.getName(), p.getNumber(), p.getPosition(), teamName),
                    team, minute, m));
        }
    }

    //tên, region 2 đội tạo Goal
    private static class TeamInfo {
        final String teamAName, teamARegion, teamBName, teamBRegion;
        TeamInfo(String an, String ar, String bn, String br) {
            teamAName = an; teamARegion = ar; teamBName = bn; teamBRegion = br;
        }
    }

    //kết quả
    public static class Podium {
        public final long champion, runnerUp, third1, third2;
        public Podium(long champion, long runnerUp, long third1, long third2) {
            this.champion = champion; this.runnerUp = runnerUp; this.third1 = third1; this.third2 = third2;
        }
    }
}
