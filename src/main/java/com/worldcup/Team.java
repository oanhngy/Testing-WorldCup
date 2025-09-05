package com.worldcup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Team {
    private long id;
    private String name;
    private String region;    
    private String Coach;
    private String medicalTeam;

    //Danh sách thành viên
    private final List<Player> players = new ArrayList<>();
    private final List<String> assistantCoaches = new ArrayList<>();

    //Chỉ số thi đấu
    private int points = 0;         //điểm tích lũy ở vòng bảng
    private int goalDifference = 0; //hiệu số(GF - GA), tích lũy
    private boolean host;

    public Team(String name) {
        this.name = name;
    }

    public Team(String name, String region, String coach, String medicalTeam) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (isBlank(region)) {
            throw new IllegalArgumentException("region must not be blank");
        }
        this.name = name;
        this.region = region;
        this.Coach = coach;
        this.medicalTeam = medicalTeam;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    //Getters/Setters tối thiểu
    public String getName() { return name; }
    public String getRegion() { return region; }
    public String getCoach() { return Coach; }
    public String getMedicalStaff() { return medicalTeam; }
    public boolean isHost() { return host; }  
    public long getId() { return id; }

    public void setName(String name) { this.name = name; }
    public void setHost(boolean host) { this.host = host; }
    public void setCoach(String coach) { this.Coach = coach; }
    public void setMedicalStaff(String staff) { this.medicalTeam = staff; }
    public void setId(long id) { this.id = id; }
    
    //Quản lý cầu thủ
    //Thêm 1 cầu thủ
    public boolean addPlayer(Player p) {
        if (p == null) return false;
        if (players.size() >= 22) return false;
        players.add(p);
        return true;
    }

    public boolean removePlayer(Player p) {
        return players.remove(p);
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public int getPlayerCount() {
        return players.size();
    }

    //Trợ lý HLV
    public boolean addAssistantCoach(String name) {
        if (name == null || name.isEmpty()) return false;
        if (assistantCoaches.size() >= 3) return false;
        assistantCoaches.add(name);
        return true;
    }

    public List<String> getAssistantCoaches() {
        return Collections.unmodifiableList(assistantCoaches);
    }

    //Điểm số & hiệu số
    public void addPoints(int delta) {
        this.points += delta;
    }

    public int getPoints() {
        return points;
    }

    //Cập nhật hiệu số tích lũy theo GF-GA
    public void updateGoalDifference(int goalsFor, int goalsAgainst) {
        this.goalDifference += (goalsFor - goalsAgainst);
    }

    public int getGoalDifference() {
        return goalDifference;
    }

    //Thống kê thẻ của đội 
    public int getTotalYellowCards() {
        int sum = 0;
        for (Player p : players) {
            sum += p.getYellowCards();
        }
        return sum;
    }

    public int getTotalRedCards() {
        int sum = 0;
        for (Player p : players) {
            sum += p.getRedCards();
        }
        return sum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Team)) return false;
        Team other = (Team) o;
        return Objects.equals(name, other.name) &&
               Objects.equals(region, other.region);
    }
    @Override
    public int hashCode() {
        return Objects.hash(name, region);
    }
    @Override
    public String toString() {
        return name;
    }
}
