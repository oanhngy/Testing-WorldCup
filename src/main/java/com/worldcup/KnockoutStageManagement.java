package com.worldcup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class KnockoutStageManagement {
    public static class BracketInfo {
        private final List<Match> roundOf16=new ArrayList<>(8);
        private final List<Match> quarterFinals=new ArrayList<>(4);
        private final List<Match> semiFinals=new ArrayList<>(2);
        private Match finalMatch;

        private Team champion;
        private Team silver;
        private final List<Team> bronzes=new ArrayList<>(2);

        public List<Match> getRoundOf16() {
            return roundOf16;
        }

        public List<Match> getQuarterFinals() {
            return quarterFinals;
        }

        public List<Match> getSemiFinals() {
            return semiFinals;
        }

        public Match getFinalMatch() {
            return finalMatch;
        }

        public Team getChampion() {
            return champion;
        }

        public Team getSilver() {
            return silver;
        }

        public List<Team> getBronzes() {
            return Collections.unmodifiableList(bronzes);
        }

        public void setFinalMatch(Match m) {
            this.finalMatch=m;
        }

        private void setChampion(Team t) {
            this.champion=t;
        }

        private void setSilvers(Team t) {
            this.silver=t;
        }

        private void setBronzes(Team b1, Team b2) {
            bronzes.clear();
            if(b1!=null) bronzes.add(b1);
            if(b2!=null) bronzes.add(b2);
        }

        //tóm tắt bracket
        public String snapshot() {
            StringBuilder sb=new StringBuilder();
            sb.append("Round of 16:\n");
            for(int i=0; i<roundOf16.size(); i++) {
                Match m= roundOf16.get(i);
                sb.append(String.format("  %d) %s vs %s\n", i + 1, m.getTeamA().getName(), m.getTeamB().getName()));
            }

            sb.append("Quarter-Finals (Q1...Q4):\n");
            for(int i=0; i<quarterFinals.size(); i++) {
                Match m=quarterFinals.get(i);
                sb.append(String.format("  Q%d) %s vs %s\n", i + 1, m.getTeamA().getName(), m.getTeamB().getName()));
            }

            sb.append("Semi-Finals(S1...S2):\n");
            for(int i=0; i<semiFinals.size(); i++) {
                Match m=semiFinals.get(i);
                sb.append(String.format("  S%d) %s vs %s\n", i + 1, m.getTeamA().getName(), m.getTeamB().getName()));
            }

            if (finalMatch != null) {
                sb.append(String.format("Final: %s vs %s\n", finalMatch.getTeamA().getName(), finalMatch.getTeamB().getName()));
            }

            if(champion!=null) {
                sb.append(String.format("Champion (Gold): %s\n", champion.getName()));
            }

            if (silver != null) {
                sb.append(String.format("Silver: %s\n", silver.getName()));
            }
                
            if (!bronzes.isEmpty()) {
                sb.append("Bronzes: ").append(
                    bronzes.stream().map(Team::getName).collect(Collectors.joining(", "))).append("\n");
            }
            return sb.toString();
        }
    }
    
    //=====instance state
    private final BracketInfo bracket=new BracketInfo();
    private final Tournament tournament;

    public KnockoutStageManagement(Tournament tournament) {
        this.tournament=Objects.requireNonNull(tournament, "Tournament khong duoc null!");
    }

    public BracketInfo getBracket() {
        return bracket;
    }

    //=====entrypoints
    //BR12-top16
    public void seedRoundOf16FromGroups() {
        List<Group> groups = ensureGroups(tournament);
        Map<String, Group> byName = groups.stream().collect(Collectors.toMap(Group::getGroupName, g -> g));

        //lấy top2 từng bảng
        Team A1=top(byName, "A", 0), A2=top(byName,"A",1);
        Team B1=top(byName, "B", 0), B2=top(byName,"B",1);
        Team C1=top(byName, "C", 0), C2=top(byName,"C",1);
        Team D1=top(byName, "D", 0), D2=top(byName,"D",1);
        Team E1=top(byName, "E", 0), E2=top(byName,"E",1);
        Team F1=top(byName, "F", 0), F2=top(byName,"F",1);
        Team G1=top(byName, "G", 0), G2=top(byName,"G",1);
        Team H1=top(byName, "H", 0), H2=top(byName,"H",1);

        bracket.getRoundOf16().clear();

        //tạo trận
        bracket.getRoundOf16().add(new Match(A1, B2, true)); 
        bracket.getRoundOf16().add(new Match(B1, A2, true)); 
        bracket.getRoundOf16().add(new Match(C1, D2, true)); 
        bracket.getRoundOf16().add(new Match(D1, C2, true)); 
        bracket.getRoundOf16().add(new Match(E1, F2, true));
        bracket.getRoundOf16().add(new Match(F1, E2, true));
        bracket.getRoundOf16().add(new Match(G1, H2, true)); 
        bracket.getRoundOf16().add(new Match(H1, G2, true));
    }

    //BR13-quarterfinal
    public void buildQuarterFinals() {
        ensureFinished(bracket.getRoundOf16(), "Round of 16");

        Team W1 = winnerOf(bracket.getRoundOf16().get(0));
        Team W2 = winnerOf(bracket.getRoundOf16().get(1));
        Team W3 = winnerOf(bracket.getRoundOf16().get(2));
        Team W4 = winnerOf(bracket.getRoundOf16().get(3));
        Team W5 = winnerOf(bracket.getRoundOf16().get(4));
        Team W6 = winnerOf(bracket.getRoundOf16().get(5));
        Team W7 = winnerOf(bracket.getRoundOf16().get(6));
        Team W8 = winnerOf(bracket.getRoundOf16().get(7));

        bracket.getQuarterFinals().clear();
        bracket.getQuarterFinals().add(new Match(W1, W2, true)); 
        bracket.getQuarterFinals().add(new Match(W3, W4, true)); 
        bracket.getQuarterFinals().add(new Match(W5, W6, true));
        bracket.getQuarterFinals().add(new Match(W7, W8, true)); 
    }

    //BR14-semifinal
    public void buildSemiFinals() {
        ensureFinished(bracket.getQuarterFinals(), "Quarter-finals");
        Team WQ1 = winnerOf(bracket.getQuarterFinals().get(0));
        Team WQ2 = winnerOf(bracket.getQuarterFinals().get(1));
        Team WQ3 = winnerOf(bracket.getQuarterFinals().get(2));
        Team WQ4 = winnerOf(bracket.getQuarterFinals().get(3));

        bracket.getSemiFinals().clear();
        bracket.getSemiFinals().add(new Match(WQ1, WQ2, true));
        bracket.getSemiFinals().add(new Match(WQ3, WQ4, true));
    }

    //BR15-final
    public void buildFinal() {
        ensureFinished(bracket.getSemiFinals(), "Semi-finals");
        Team WS1 = winnerOf(bracket.getSemiFinals().get(0));
        Team WS2 = winnerOf(bracket.getSemiFinals().get(1));
        bracket.setFinalMatch(new Match(WS1, WS2, true));
    }

    //xác định thứ hạng sau final
    public void resolveMedals() {
        Match finalMatch=bracket.getFinalMatch();
        if(finalMatch==null) {
            throw new IllegalStateException("Tran chung ket chua duoc tao!");
        }
        if(!finalMatch.isFinished()) {
            throw new IllegalStateException("Tran chung ket chua co ket qua!");
        }
        Team gold=winnerOf(finalMatch);
        Team silver=loserOf(finalMatch);

        //đồng
        Team bronze1=loserOf(bracket.getSemiFinals().get(0));
        Team bronze2=loserOf(bracket.getSemiFinals().get(1));

        bracket.setChampion(gold);
        bracket.setSilvers(silver);
        bracket.setBronzes(bronze1, bronze2);
        if(tournament!=null) {
            tournament.setChampion(gold);
        }
    }



    //=====Helpers
    private static List<Group> ensureGroups(Tournament t) {
        if(t==null || t.getGroups()==null || t.getGroups().size()!=8) {
            throw new IllegalArgumentException("Tournament phai co dung 8 bang(A-H) truoc khi seed Round of 16!");
        }
        return t.getGroups();
    }

    private static Team top(Map<String, Group>byName, String group, int index) {
        Group g=byName.get(group);
        if(g==null) throw new IllegalArgumentException("Thieu bang "+group);
        List<Team> top2=g.getTop2Teams();
        if(top2.size()<2) {
            throw new IllegalArgumentException("Bang "+group+ "chua co day du Top 2!"); 
        }
        return top2.get(index);
    }

    private static void ensureFinished(List<Match> matches, String roundName) {
        if(matches==null || matches.isEmpty()) {
            throw new IllegalStateException(roundName+" chua duoc tao!");
        }
        for(Match m:matches) {
            if(m==null || !m.isFinished()) {
                throw new IllegalStateException(roundName+ " chua hoan tat!");
            }
            if(m.getGoalsTeamA()==m.getGoalsTeamB()) {
                throw new IllegalStateException(roundName+ " co tran hoa, CAN XU LY DA LUAN LUU/ HIEP PHU!");
            }
        }
    }

    private static Team winnerOf(Match m) {
        if(!m.isFinished()) throw new IllegalStateException("Tran dau chua ket thuc!");
        if(m.getGoalsTeamA()==m.getGoalsTeamB()) {
            throw new IllegalStateException("Tran hoa - chua ho tro luan luu trong Match!");
        }
        return (m.getGoalsTeamA()>m.getGoalsTeamB()? m.getTeamA():m.getTeamB());
    }

    private static Team loserOf(Match m) {
        return (m.getGoalsTeamA()> m.getGoalsTeamB())? m.getTeamB():m.getTeamA();
    }
    
}

