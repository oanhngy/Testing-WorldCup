package com.worldcup;

public class Player {
    //cố định
    private String name;
    private int number;
    private String position;
    private String teamName;
    //thống kê cá nhân
    private int goals;
    private int yellowCards;
    private int redCards;

    public Player(String name, int number, String position, String teamName) {
        if (number < 1 || number > 26)
            throw new IllegalArgumentException("So ao phai tu 1 den 26!");
        this.name = name;
        this.number = number;
        this.position = position;
        this.teamName = teamName;
        this.goals = 0;
        this.yellowCards = 0;
        this.redCards = 0;
    }

    public String getName() { return name; }
    public int getNumber() { return number; }
    public String getPosition() { return position; }
    public String getTeamName() { return teamName; }
    public int getGoals() { return goals; }
    public int getYellowCards() { return yellowCards; }
    public int getRedCards() { return redCards; }

    //ghi bàn thắng
    public void scoreGoal() {
        goals++;
    }

    //thẻ vàng
    public void addYellowCard() {
        yellowCards++;
    }

    //thẻ đỏ
    public void addRedCard() {
        redCards++;
    }
}
