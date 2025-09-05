package com.worldcup;

public class Goal {
    private Player player;
    private Team team;
    private int minute;
    private Match match;

    public Goal(Player player, Team team, int minute, Match match) {
        if (player == null) {
            throw new IllegalArgumentException("Player khong duoc null!");
        }
        if (team == null) {
            throw new IllegalArgumentException("Team khong duoc null!");
        }
        if (match == null) {
            throw new IllegalArgumentException("Match khong duoc null!");
        }
        if (minute <= 0 || minute > 120) {
            throw new IllegalArgumentException("Thoi gian ghi ban khong hop le (0-120 phut)");
        }
        this.player = player;
        this.team = team;
        this.minute = minute;
        this.match = match;
    }

    public Player getPlayer() {
        return player;
    }

    public Team getTeam() {
        return team;
    }

    public int getMinute() {
        return minute;
    }

    public Match getMatch() {
        return match;
    }

    @Override
    public String toString() {
        return String.format("Goal by %s (%s) at %d' in match %s vs %s",
                player.getName(), team.getName(), minute,
                match.getTeamA().getName(), match.getTeamB().getName());
    }
}
