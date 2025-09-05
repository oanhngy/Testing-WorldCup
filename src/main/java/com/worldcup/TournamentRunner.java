package com.worldcup;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TournamentRunner {
    public static void main(String[] args) throws Exception {

        SimpleDB.init();
        ensureSeededWithGroupsAndFixtures();
        printGroupsAndTeams();
        printGroupStageFixtures();
        simulateAllScheduledGroupMatches();
        printStandingsForAllGroups();
        printTop2();
        runKnockoutBracketWithPrinting();
        System.out.println("Hoan tat mo phong vong bang.");
    }

        //seeding (nếu gr đang rỗng), skip nếu đã có
        private static void ensureSeededWithGroupsAndFixtures() throws Exception {
            if (countTable("groups") > 0) {
                System.out.println("DB da co du lieu groups -> bo qua seed.");
                return;
            }
            System.out.println("DB chua co du lieu -> dang seed 32 doi + chia 8 bang + sinh lich.");

            //chia bảng từ dsach 32 đội trong SampleTeams
            GroupService gs = new GroupService();
            List<Long> groupIds = gs.createGroups(SampleTeams.thirtyTwoTeams());

            //tạo 6 trận/bảng
            FixtureGenerator fg = new FixtureGenerator();
            for (Long gid : groupIds) {
                List<Long> mids = fg.generateGroupFixtures(gid);
                System.out.println("Da sinh " + mids.size() + " tran cho Group id=" + gid);
            }
            System.out.println("Hoan tat seed & tao lich vong bang.");
        }

        //similation, mô phỏng kqua các trận
        private static void simulateAllScheduledGroupMatches() throws Exception {
            List<Long> matchIds = listScheduledGroupMatches();
            System.out.println("Co " + matchIds.size() + " tran vong bang can mo phong.");

            MatchService ms = new MatchService();
            int i = 1;
            for (Long mid : matchIds) {
                int[] score = ms.simulateGroupMatch(mid);
                String[] names = getTeamNames(mid);
                System.out.printf("(%02d) %s %d - %d %s  [matchId=%d]%n",
                        i++, names[0], score[0], score[1], names[1], mid);
            }
        }


        //==== PRINTING HELPERS ====
        //in dsach đội theo từng bảng
        private static void printGroupsAndTeams() throws SQLException {
            Map<Long, String> groups = loadGroupsOrdered();
            System.out.println("=== DANH SACH DOI THEO BANG ===");
            for (Map.Entry<Long, String> e : groups.entrySet()) {
                long gid = e.getKey();
                String gname = e.getValue();
                List<String> teams = loadTeamNamesInGroup(gid);
                System.out.printf("Group %s: %s%n", gname, String.join(", ", teams));
            }
            System.out.println();
        }

        //in lịch đấu theo từng bảng
        private static void printGroupStageFixtures() throws SQLException {
            Map<Long, String> groups = loadGroupsOrdered();
            System.out.println("=== LICH THI DAU VONG BANG ===");
            for (Map.Entry<Long, String> e : groups.entrySet()) {
                long gid = e.getKey();
                String gname = e.getValue();
                System.out.printf("Group %s:%n", gname);
                List<String> lines = loadFixturesByGroup(gid);
                for (String line : lines) System.out.println("  " + line);
            }
            System.out.println();
        } 
        
        //in BXH từng bảng vs Pts,GD,Cards
        private static void printStandingsForAllGroups() throws Exception {
            StandingService ss = new StandingService();
            Map<Long, String> groups = loadGroupsOrdered();
            System.out.println("\n=== BANG XEP HANG VONG BANG ===");
            for (Map.Entry<Long, String> e : groups.entrySet()) {
                long gid = e.getKey();
                String gname = e.getValue();
                System.out.printf("Group %s:%n", gname);
                var table = ss.getGroupTable(gid);
                int rank = 1;
                for (StandingService.Row r : table) {
                    System.out.printf("  %d) %s%n", rank++, r);
                }
            }
        }

        //in Top2
        private static void printTop2() throws Exception {
            StandingService ss = new StandingService();
            Map<Long, String> groups = loadGroupsOrdered();
            var top = ss.top2EachGroup();
            System.out.println("\n=== TOP 2 MOI BANG (A1..H2) ===");
            int idx = 0;
            for (Map.Entry<Long, String> e : groups.entrySet()) {
                long gid = e.getKey();
                String gname = e.getValue();
                long t1 = top.get(idx++);
                long t2 = top.get(idx++);
                String n1 = getTeamNameById(t1);
                String n2 = getTeamNameById(t2);
                System.out.printf("%s1: %s   %s2: %s%n", gname, n1, gname, n2);
            }
        }

        //in team theo id
        private static String getTeamNameById(long teamId) throws SQLException {
            try (Connection c = SimpleDB.get();
                PreparedStatement ps = c.prepareStatement("select name from teams where id=?")) {
                ps.setLong(1, teamId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new SQLException("Khong tim thay team id=" + teamId);
                    return rs.getString(1);
                }
            }
        }

        //map A-H
        private static Map<String, long[]> buildTop2Map() throws Exception {
            StandingService ss = new StandingService();
            var groups = loadGroupsOrdered();
            var top = ss.top2EachGroup(); 

            Map<String, long[]> map = new LinkedHashMap<>();
            int idx = 0;
            for (String gname : groups.values()) { 
                long first = top.get(idx++);
                long second = top.get(idx++);
                map.put(gname, new long[]{first, second});
            }
            return map;
        }

        // //chạy toàn bộ knock-out, in hạng final
        // private static void runKnockoutBracket() throws Exception {
        //     Map<String, long[]> top2 = buildTop2Map();
        //     BracketService bs = new BracketService();

        //     System.out.println("\n=== KNOCK-OUT BRACKET ===");
        //     BracketService.Podium podium = bs.runElimination(top2);

        //     String champion = getTeamNameById(podium.champion);
        //     String runnerUp = getTeamNameById(podium.runnerUp);
        //     String third1   = getTeamNameById(podium.third1);
        //     String third2   = getTeamNameById(podium.third2);

        //     System.out.println("\n=== KET QUA CHUNG CUOC ===");
        //     System.out.println("HANG 1: " + champion);
        //     System.out.println("HANG 2: " + runnerUp);
        //     System.out.println("DONG HANG 3: " + third1 + ", " + third2);
        // }

        //chạy knockout round+ in
        private static void runKnockoutBracketWithPrinting() throws Exception {
            System.out.println("\n=== KNOCK-OUT BRACKET ===");

            Map<String, long[]> top2 = buildTop2Map();
            BracketService bs = new BracketService();

            //r16
            List<Long> r16 = bs.createRoundOf16(top2);
            List<Long> winR16 = simulateRoundPrint("ROUND OF 16", r16, bs);

            //r8
            List<Long> qf = createNextRoundMatches(winR16);
            List<Long> winQF = simulateRoundPrint("QUARTER-FINALS", qf, bs);

            //semi
            List<Long> sf = createNextRoundMatches(winQF);
            RoundResult sfRes = simulateRoundPrintWithLosers("SEMI-FINALS", sf, bs);

            //hạng 3
            List<Long> p3 = new ArrayList<>(1);
            p3.add(createKoMatch(sfRes.losers.get(0), sfRes.losers.get(1)));
            simulateRoundPrint("THIRD-PLACE", p3, bs);

            //final
            List<Long> fin = new ArrayList<>(1);
            fin.add(createKoMatch(sfRes.winners.get(0), sfRes.winners.get(1)));
            List<Long> finWinners = simulateRoundPrint("FINAL", fin, bs);

            //In podium
            long champion = finWinners.get(0);
            long runnerUp = getOpponent(fin.get(0), champion);
            long third1 = winnerOf(p3.get(0));
            long third2 = getOpponent(p3.get(0), third1);

            System.out.println("\n=== KET QUA CHUNG CUOC ===");
            System.out.println("HANG 1: " + getTeamNameById(champion));
            System.out.println("HANG 2: " + getTeamNameById(runnerUp));
            System.out.println("DONG HANG 3: " + getTeamNameById(third1) + ", " + getTeamNameById(third2));
        }

        //mô phỏng, in 1 vòng KO, trả về winner
        private static List<Long> simulateRoundPrint(String title, List<Long> matchIds, BracketService bs) throws Exception {
            System.out.println("\n-- " + title + " --");
            List<Long> winners = new ArrayList<>(matchIds.size());
            for (Long mid : matchIds) {
                bs.simulateKnockoutMatch(mid);
                ResultLine rl = loadResultLine(mid);
                System.out.printf("%s %d - %d %s  [matchId=%d]%n", rl.aName, rl.ga, rl.gb, rl.bName, mid);
                winners.add(rl.ga > rl.gb ? rl.aId : rl.bId);
            }
            return winners;
        }

        //in bán kết
        private static RoundResult simulateRoundPrintWithLosers(String title, List<Long> matchIds, BracketService bs) throws Exception {
            System.out.println("\n-- " + title + " --");
            List<Long> winners = new ArrayList<>(2);
            List<Long> losers  = new ArrayList<>(2);
            for (Long mid : matchIds) {
                bs.simulateKnockoutMatch(mid);
                ResultLine rl = loadResultLine(mid);
                System.out.printf("%s %d - %d %s  [matchId=%d]%n", rl.aName, rl.ga, rl.gb, rl.bName, mid);
                if (rl.ga > rl.gb) { winners.add(rl.aId); losers.add(rl.bId); }
                else { winners.add(rl.bId); losers.add(rl.aId); }
            }
            return new RoundResult(winners, losers);
        }

        //tạo lượt KO tiếp theo từ winner
        private static List<Long> createNextRoundMatches(List<Long> winners) throws SQLException {
            List<Long> mids = new ArrayList<>(winners.size() / 2);
            for (int i = 0; i < winners.size(); i += 2) {
                mids.add(createKoMatch(winners.get(i), winners.get(i + 1)));
            }
            return mids;
        }

        //tạo trận KO
        private static long createKoMatch(long teamAId, long teamBId) throws SQLException {
            return new MatchDAO().insert(null, teamAId, teamBId, true);
        }

        //lấy team còn lại torng 1 match
        private static long getOpponent(long matchId, long knownTeamId) throws SQLException {
            final String sql = "select team_a_id, team_b_id from matches where id=?";
            try (Connection c = SimpleDB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setLong(1, matchId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    long ta = rs.getLong(1), tb = rs.getLong(2);
                    return (ta == knownTeamId) ? tb : ta;
                }
            }
        }

        //winner 1 match
        private static long winnerOf(long matchId) throws SQLException {
            final String sql = "select team_a_id, team_b_id, goals_a, goals_b from matches where id=?";
            try (Connection c = SimpleDB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setLong(1, matchId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    long ta = rs.getLong(1), tb = rs.getLong(2);
                    int ga = rs.getInt(3), gb = rs.getInt(4);
                    return (ga > gb) ? ta : tb;
                }
            }
        }

        //dòng kết quả in trận KO
        private static ResultLine loadResultLine(long matchId) throws SQLException {
            final String sql = """
                select ta.id, ta.name, tb.id, tb.name, m.goals_a, m.goals_b
                from matches m
                join teams ta on ta.id = m.team_a_id
                join teams tb on tb.id = m.team_b_id
                where m.id = ?
                """;
            try (Connection c = SimpleDB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setLong(1, matchId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return new ResultLine(
                        rs.getLong(1), rs.getString(2),
                        rs.getLong(3), rs.getString(4),
                        rs.getInt(5), rs.getInt(6)
                    );
                }
            }
        }

        //DTO in kqua
        private static class ResultLine {
            final long aId, bId; final String aName, bName; final int ga, gb;
            ResultLine(long aId, String aName, long bId, String bName, int ga, int gb) {
                this.aId = aId; this.aName = aName; this.bId = bId; this.bName = bName; this.ga = ga; this.gb = gb;
            }
        }

        //DTO trả win, lose cho bán kết
        private static class RoundResult {
            final List<Long> winners, losers;
            RoundResult(List<Long> w, List<Long> l) { winners = w; losers = l; }
        }



        //==== DB HELPERS ====
        //truy vấn tên 2 đội theo matchId
        private static String[] getTeamNames(long matchId) throws SQLException {
            final String sql = """
                select ta.name, tb.name
                from matches m
                join teams ta on ta.id = m.team_a_id
                join teams tb on tb.id = m.team_b_id
                where m.id = ?
                """;
            try (Connection c = SimpleDB.get();
                PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setLong(1, matchId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new SQLException("Khong tim thay match id=" + matchId);
                    return new String[]{ rs.getString(1), rs.getString(2) };
                }
            }
        }

        //lấy dsach match id còn scheduled (is_knockout=false, status=scheduled)
        private static List<Long> listScheduledGroupMatches() throws SQLException {
            final String sql = """
                select id
                from matches
                where is_knockout = 0
                and status = 'SCHEDULED'
                order by id asc
                """;
            List<Long> out = new ArrayList<>();
            try (Connection c = SimpleDB.get();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getLong(1));
            }
            return out;
        }

        //check db trống hay k
        private static int countTable(String table) throws SQLException {
            try (Connection c = SimpleDB.get();
                PreparedStatement ps = c.prepareStatement("select count(*) from " + table);
                ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }

        //lấy map group A-H
        private static Map<Long, String> loadGroupsOrdered() throws SQLException {
            final String sql = "select id, name from groups order by name asc";
            Map<Long, String> out = new LinkedHashMap<>();
            try (Connection c = SimpleDB.get();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.put(rs.getLong(1), rs.getString(2));
                }
            }
            return out;
        }

        //lấy dsach tên đội trong 1 gr
        private static List<String> loadTeamNamesInGroup(long groupId) throws SQLException {
            final String sql = """
                select t.name
                from group_teams gt
                join teams t on t.id = gt.team_id
                where gt.group_id = ?
                order by t.name asc
                """;
            List<String> names = new ArrayList<>();
            try (Connection c = SimpleDB.get();
                PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setLong(1, groupId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) names.add(rs.getString(1));
                }
            }
            return names;
        }

        //lấy dsach dòng mô tả fixture cho gr
        private static List<String> loadFixturesByGroup(long groupId) throws SQLException {
            final String sql = """
                select m.id, ta.name, tb.name, m.status, m.goals_a, m.goals_b
                from matches m
                join teams ta on ta.id = m.team_a_id
                join teams tb on tb.id = m.team_b_id
                where m.group_id = ?
                order by m.id asc
                """;
            List<String> lines = new ArrayList<>();
            try (Connection c = SimpleDB.get();
                PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setLong(1, groupId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        long mid = rs.getLong(1);
                        String a = rs.getString(2);
                        String b = rs.getString(3);
                        String status = rs.getString(4);
                        int ga = rs.getInt(5);
                        int gb = rs.getInt(6);
                        String scoreOrStatus = "SCHEDULED".equals(status) ? status
                                : (ga + " - " + gb);
                        lines.add(String.format("%s vs %s  [matchId=%d]  %s", a, b, mid, scoreOrStatus));
                    }
                }
            }
            return lines;
        }
}
