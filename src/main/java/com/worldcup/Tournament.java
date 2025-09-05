package com.worldcup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Tournament {
    private List<Team> allTeams;
    private List<Team> qualifiedTeams;
    private List<Group> groups;
    private List<Match> allMatches;
    private Team hostTeam;
    private Team champion;
    private List<Player> allPlayers;
    private List<Goal> allGoals; //* */

    public Tournament(Team hostTeam) {
        if (hostTeam == null) {
            throw new IllegalArgumentException("Host team khong duoc null!");
        }
        this.allTeams = new ArrayList<>();
        this.qualifiedTeams=new ArrayList<>();
        this.groups = new ArrayList<>();
        this.allMatches = new ArrayList<>();
        this.hostTeam = hostTeam;
        this.champion = null;
        this.allPlayers = new ArrayList<>();
        this.allGoals=new ArrayList<>(); //* */
    }

    //BR1,2: vòng playoff
    public void conductPlayoffs(List<Team>asia, List<Team>concacaf, List<Team>conmebol, List<Team> oceania) {
        if (asia == null || asia.size() < 6) {
            throw new IllegalArgumentException("Asia phai co it nhat 6 doi!");
        }
        if (concacaf == null || concacaf.size() < 4) {
            throw new IllegalArgumentException("Concacaf phai co it nhat 4 doi!");
        }
        if (conmebol == null || conmebol.size() < 5) {
            throw new IllegalArgumentException("Conmebol phai co it nhat 5 doi!");
        }
        if (oceania == null || oceania.size() < 1) {
            throw new IllegalArgumentException("Oceania phai co it nhat 1 doi!");
        }

        List<Team> qualified=new ArrayList<>();
        Team asiaTeam=asia.get(5);
        Team concacafTeam=concacaf.get(3);
        PlayoffMatch playoff1FirstLeg=new PlayoffMatch(asiaTeam,concacafTeam,true,"Asia","Concacaf");
        PlayoffMatch playoff1SecondLeg=new PlayoffMatch(asiaTeam, concacafTeam, false, "Asia", "Concacaf");
        playoff1FirstLeg.setSecondLeg(playoff1SecondLeg);

        Team conmebolTeam=asia.get(4);
        Team oceaniaTeam=concacaf.get(0);
        PlayoffMatch playoff2FirstLeg=new PlayoffMatch(conmebolTeam,oceaniaTeam,true,"Conmebol","Oceania");
        PlayoffMatch playoff2SecondLeg=new PlayoffMatch(conmebolTeam, oceaniaTeam, false, "Conmebol", "Oceania");
        playoff2FirstLeg.setSecondLeg(playoff2SecondLeg);

        allMatches.add(playoff1FirstLeg);
        allMatches.add(playoff1SecondLeg);
        allMatches.add(playoff2FirstLeg);
        allMatches.add(playoff2SecondLeg);
    }

    //chia 32 đội thàng 8 bảng, k đc trùng tên
    public void assignGroups(List<Team> preQualifiedTeams) {
        List<Team> fullList=new ArrayList<>(preQualifiedTeams);
        if (fullList.contains(hostTeam)) {
            throw new IllegalArgumentException("Host team da ton tai trong danh sach preQualifiedTeams!");
        }
        fullList.add(hostTeam);
        if(fullList.size()!=32) {
            throw new IllegalArgumentException("Can du 32 doi de chia bang!");
        }
        Collections.shuffle(fullList); //chọn random

        Group.GroupName[] groupNames = Group.GroupName.values();
        for(int i=0; i<8; i++) {
            List<Team> groupTeams=fullList.subList(i*4, (i+1)*4);
            Group group = new Group(groupNames[i].name(), new ArrayList<>(groupTeams));
            groups.add(group);
        }
    }

    //BR11: thêm bàn thắng
    public void addGoal(Goal goal) {
        if(goal!=null) {
            allGoals.add(goal);
        }
    }

    //BR11: vua phá lưới
    public List<Player> getTopScorers() {
        Map<Player, Long> goalCount = allGoals.stream()
                .collect(Collectors.groupingBy(Goal::getPlayer, Collectors.counting()));

        long maxGoals = goalCount.values().stream()
                .max(Long::compare)
                .orElse(0L);

        return goalCount.entrySet().stream()
                .filter(entry -> entry.getValue() == maxGoals)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    

    public List<Team> getAllTeams() {
        return allTeams;
    }

    public List<Team> getQualifiedTeams() {
        return qualifiedTeams;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public List<Match> getAllMatches() {
        return allMatches;
    }

    public Team getHostTeam() {
        return hostTeam;
    }

    public Team getChampion() {
        return champion;
    }

    public List<Player> getAllPlayers() {
        return allPlayers;
    }

    public void setChampion(Team champion) {
        this.champion = champion;
    }

    public void addPlayer(Player player) {
        this.allPlayers.add(player);
    }

    public void addMatch(Match match) {
        this.allMatches.add(match);
    }
}
