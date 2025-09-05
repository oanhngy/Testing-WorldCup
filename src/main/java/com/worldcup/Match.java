package com.worldcup;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Match {
    private Team teamA;
    private Team teamB;
    private int goalsTeamA;
    private int goalsTeamB;
    private List<Player> goalScorers; //ai ghi bàn
    private boolean isKnockout;
    private boolean isFinished;
    //cầu thủ main, dự bị
    private List<Player> mainPlayersTeamA = new ArrayList<>();
    private List<Player> mainPlayersTeamB = new ArrayList<>();
    private List<Player> substitutePlayersTeamA = new ArrayList<>();
    private List<Player> substitutePlayersTeamB = new ArrayList<>();
    //thời gian đấu
    private final int regularHalfMinutes = 45;
    private final int halfTimeBreakMinutes = 15;
    private final int extraTimeMinutes = 30;
    //thẻ đỏ, vàng, loại
    private Map<Player, Integer> yellowCardsThisMatch = new HashMap<>();
    private Set<Player> disqualifiedPlayers = new HashSet<>();
    //thay người
    private List<Map.Entry<Player, Player>> substitutions = new ArrayList<>();
    private int substitutionCount = 0;

    public Match(Team teamA, Team teamB, boolean isKnockout) {
        if (teamA == null || teamB == null) {
        throw new IllegalArgumentException("Doi khong duoc null!");
        }
        this.teamA = teamA;
        this.teamB = teamB;
        this.goalsTeamA = 0;
        this.goalsTeamB = 0;
        this.isKnockout = isKnockout;
        this.isFinished = false;
        this.goalScorers = new ArrayList<>();
    }

    public Match(Team teamA, Team teamB, boolean isKnockout, boolean isFinished) {
        this.teamA=teamA;
        this.teamB=teamB;
        this.isKnockout=isKnockout;
        this.isFinished=isFinished;
    }

    public Match(Team teamA, Team teamB) {
        this(teamA, teamB, false);
    }

    public Team getTeamA() {
        return teamA;
    }

    public Team getTeamB() {
        return teamB;
    }

    public int getGoalsTeamA() {
        return goalsTeamA;
    }

    public int getGoalsTeamB() {
        return goalsTeamB;
    }

    public List<Player> getGoalScorers() {
        return goalScorers;
    }

    public boolean isKnockout() {
        return isKnockout;
    }

    public void setKnockout(boolean isKnockout) {
        this.isKnockout=isKnockout;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setIsFinished(boolean isFinished) {
        this.isFinished = isFinished;
    }

    public void setGoalsTeamA(int goals) {
        this.goalsTeamA = goals;
    }

    public void setGoalsTeamB(int goals) {
        this.goalsTeamB = goals;
    }


    //cầu thủ ghi bàn, update tỉ số+danh sách ghi bàn
    public void scoreGoal(Player player) {
        if (!player.getTeamName().equals(teamA.getName()) && !player.getTeamName().equals(teamB.getName())) {
            throw new IllegalArgumentException("Cau thu khong thuoc doi nao trong tran dau!");
        }

        player.scoreGoal();
        goalScorers.add(player);

        if (player.getTeamName().equals(teamA.getName())) {
            goalsTeamA++;
        } else {
            goalsTeamB++;
        }
    }

    //thẻ vàng, nếu 2 thẻ--> loại
    public void giveYellowCard(Player player) {
        player.addYellowCard(); //update toàn giải
        yellowCardsThisMatch.put(player, yellowCardsThisMatch.getOrDefault(player, 0) + 1);

        if (yellowCardsThisMatch.get(player) >= 2) {
            disqualifiedPlayers.add(player);
        }
    }

    //thẻ đỏi -> loại
    public void giveRedCard(Player player) {
        player.addRedCard();
        disqualifiedPlayers.add(player);
    }

    //check bị loại k
    public boolean isDisqualified(Player player) {
        return disqualifiedPlayers.contains(player);
    }

    //đếm sluong cầu thủ đủ đkien đấu
    public int countAvailablePlayers(Team team) {
        int count = 0;
        for (Player p : team.getPlayers()) {
            if (!isDisqualified(p)) count++;
        }
        return count;
    }

    public boolean checkForAutoForfeit() {
        if (countAvailablePlayers(teamA) < 7) {
            goalsTeamA = 0;
            goalsTeamB = 3;
            return true;
        } else if (countAvailablePlayers(teamB) < 7) {
            goalsTeamA = 3;
            goalsTeamB = 0;
            return true;
        }
        return false;
    }

    //thay người
    public boolean substitute(Player playerOut, Player playerIn, Team team) {
        if (substitutionCount >= 3 || playerOut == null || playerIn == null) return false;
        if (!team.getPlayers().contains(playerOut) || !team.getPlayers().contains(playerIn)) return false;
        substitutions.add(new AbstractMap.SimpleEntry<>(playerOut, playerIn));
        substitutionCount++;
        return true;
    }

    //show kết quả
    public String getResult() {
        if (!isFinished) return "Tran dau chua ket thuc.";
        if (!isKnockout) {
            if (goalsTeamA > goalsTeamB) return teamA.getName() + " thang (vong bang)!!!";
            if (goalsTeamA < goalsTeamB) return teamB.getName() + " thang (vong bang)!!!";
            return "Hoa (vong bang)!!!";
        } else {
            if (goalsTeamA > goalsTeamB) return teamA.getName() + " thang (knock-out)!!!";
            if (goalsTeamA < goalsTeamB) return teamB.getName() + " thang (knock-out)!!!";
            return "Hoa sau 90 phut (knock-out)- Tiep tuc da hiep phu!!!";
        }
    }

    public Team finishMatch() {
        if (isFinished) return null;
        boolean autoForfeit = checkForAutoForfeit();
        Team winner = null;

        if (!isKnockout) {
            updateTeamStats();
            isFinished = true;
            return null;
        }
        if (goalsTeamA == goalsTeamB) {
            winner = playExtraTime();
        } else {
            winner = goalsTeamA > goalsTeamB ? teamA : teamB;
        }
        isFinished = true;
        return winner;
    }

    //hiệp phụ( knockout)
    private Team playExtraTime() {
        Random rnd = new Random();
        int extraGoalsA = rnd.nextInt(2); // 0 hoặc 1
        int extraGoalsB = rnd.nextInt(2); // 0 hoặc 1
        goalsTeamA += extraGoalsA;
        goalsTeamB += extraGoalsB;

        if (goalsTeamA > goalsTeamB) return teamA;
        if (goalsTeamA < goalsTeamB) return teamB;
        return playPenaltyShootout();
    }

    //luân lưu
    private Team playPenaltyShootout() {
        Random rnd = new Random();
        return rnd.nextInt(2) == 0 ? teamA : teamB;
    }

    //update điểm+hiệu số(vòng bảng)
    private void updateTeamStats() {
        if (!isKnockout) {
            if (goalsTeamA > goalsTeamB) {
                teamA.addPoints(3);
            } else if (goalsTeamA < goalsTeamB) {
                teamB.addPoints(3);
            } else {
                teamA.addPoints(1);
                teamB.addPoints(1);
            }
        }
        teamA.updateGoalDifference(goalsTeamA, goalsTeamB);
        teamB.updateGoalDifference(goalsTeamB, goalsTeamA);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Match other = (Match) obj;
        boolean sameMatchDirect = this.teamA.equals(other.teamA) && this.teamB.equals(other.teamB);
        boolean sameMatchReversed = this.teamA.equals(other.teamB) && this.teamB.equals(other.teamA);
        return sameMatchDirect || sameMatchReversed;
    }

    @Override
    public int hashCode() {
        return teamA.hashCode() + teamB.hashCode();
    }

}
