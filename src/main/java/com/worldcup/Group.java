package com.worldcup;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.worldcup.GroupName.isValid;

public class Group {
    private String groupName; //A-H
    private List<Team> teams;
    private List<Match> matches;

    public Group(String groupName) {
        if(groupName==null || groupName.trim().isEmpty()) {
            throw new IllegalArgumentException("Group name khong duoc null hoac de trong!");
        }
        if(!isValid(groupName)) {
            throw new IllegalArgumentException("Group name khong hop le! Phai nam trong A-H!");
        }
        this.groupName=groupName;
        this.teams=new ArrayList<>();
        this.matches=new ArrayList<>();
    }

    public Group(String groupName, List<Team> teams) {
        if(groupName == null || groupName.trim().isEmpty()) {
        throw new IllegalArgumentException("Group name khong duoc null hoac de trong!");
    }
        if (!groupName.matches("[A-H]")) {
            throw new IllegalArgumentException("Group name khong hop le! Phai nam trong A-H!");
            }
            if (teams == null) {
                throw new IllegalArgumentException("Danh sach teams khong duoc null!");
            }
            if (teams.size() > 4) {
                throw new IllegalArgumentException("Mot bang chi duoc chua toi da 4 doi!");
            }
            this.groupName = groupName;
            this.teams = new ArrayList<>(teams);
            this.matches = new ArrayList<>();
    }

    public String getGroupName() {
        return groupName;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public enum GroupName {
        A,B,C,D,E,F,G,H;
    }
    

    //thêm đội vào bảng, max 4 đội/ bảng
    public boolean addTeam(Team team) {
        if(teams.size()>=4 || team==null) return false;
        return teams.add(team);
    }

    //thêm trận đấu vào dsach
    public void addMatch(Match match) {
        if(match!=null) matches.add(match);
    }

    //sắp xếp hạng trong bảng theo BR5
    public List<Team> getStandings() {
        List<Team> sorted=new ArrayList<>(teams);
        sorted.sort((t1,t2) -> {
            if(t2.getPoints()!=t1.getPoints()) return Integer.compare(t2.getPoints(), t1.getPoints());
            if(t2.getGoalDifference()!=t1.getGoalDifference()) return Integer.compare(t2.getGoalDifference(), t1.getGoalDifference());

            int t1Cards=t1.getTotalRedCards()*2 + t1.getTotalYellowCards();
            int t2Cards=t2.getTotalRedCards()*2 + t2.getTotalYellowCards();
            if(t1Cards!=t2Cards) return Integer.compare(t1Cards, t2Cards);

            Match headToHead = findMatchBetween(t1, t2);
            if(headToHead!=null && headToHead.isFinished()) {
                // int goals1=headToHead.getTeamA().equals(t1) ? headToHead.getGoalsTeamA() : headToHead.getGoalsTeamB();
                // int goals2=headToHead.getTeamB().equals(t2) ? headToHead.getGoalsTeamB() : headToHead.getGoalsTeamA();
                // if(goals1 !=goals2) return Integer.compare(goals1, goals2);
                int g1 = 0, g2 = 0;
                if (headToHead.getTeamA().equals(t1) && headToHead.getTeamB().equals(t2)) {
                    g1 = headToHead.getGoalsTeamA();
                    g2 = headToHead.getGoalsTeamB();
                } else if (headToHead.getTeamA().equals(t2) && headToHead.getTeamB().equals(t1)) {
                    g1 = headToHead.getGoalsTeamB(); // của t1
                    g2 = headToHead.getGoalsTeamA(); // của t2
                }
                if (g1 != g2) return Integer.compare(g1, g2);

            }
            return new Random().nextBoolean()? 1: -1; 
        });
        return sorted;
    }
    
    //tìm trận giữa 2 đội
    public Match findMatchBetween(Team t1, Team t2) {
        for (Match m : matches) {
            if ((m.getTeamA().equals(t1) && m.getTeamB().equals(t2)) ||
                (m.getTeamA().equals(t2) && m.getTeamB().equals(t1))) {
                return m;
            }
        }
        return null;
    }

    //lấy 2 đội cao nhất trong từng bảng bvao2 vòng trong
    public List<Team> getTop2Teams() {
        List<Team> standings=getStandings();
        return standings.subList(0, Math.min(2,standings.size()));
    }
}

//kiểm tra hợp lệ group name
enum GroupName {
    A,B,C,D,E,F,G,H;
    public static boolean  isValid(String name) {
        for(GroupName g: GroupName.values()) {
            if(g.name().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
