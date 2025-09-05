package com.worldcup;

public class PlayoffMatch extends Match {
    private boolean isFirstLeg;
    private PlayoffMatch secondLeg;
    private String regionTeamA;
    private String regionTeamB;
    private int aggregateGoalsTeamA;
    private int aggregateGoalsTeamB;
    private Team playoffWinner;

    public PlayoffMatch(Team teamA, Team teamB, boolean isFirstLeg, String regionTeamA, String regionTeamB) {
        super(teamA, teamB, false);
        this.isFirstLeg = isFirstLeg;
        this.regionTeamA = regionTeamA;
        this.regionTeamB = regionTeamB;
        this.aggregateGoalsTeamA = 0;
        this.aggregateGoalsTeamB = 0;
        this.secondLeg = null;
        this.playoffWinner = null;
    }

    //true xdinh trận lượt đi hay về
    public boolean isFirstLeg() {
        return isFirstLeg;
    }

    //trận lượt về tương ứng
    public void setSecondLeg(PlayoffMatch secondLeg) {
        this.secondLeg=secondLeg;
    }

    public PlayoffMatch getSecondLeg() {
        return secondLeg;
    }

    //khu vực teamA
    public String getRegionTeamA() {
        return regionTeamA;
    }

    //region teamB
    public String getRegionTeamB() {
        return regionTeamB;
    }

    //tổng bàn thắng teamA,B sau 2 trận
    public int getAggregateGoalsTeamA() {
        return aggregateGoalsTeamA;
    }

    public int getAggregateGoalsTeamB() {
        return aggregateGoalsTeamB;
    }

    public Team getPlayoffWinner() {
        return playoffWinner;
    }

    //xdinh đội thắng sau 2 lượt. Gọi sau đấu 2 trận
    public void determinePlayoffWinner() {
        if(this.isFirstLeg && secondLeg!=null && this.isFinished() && secondLeg.isFinished()) {
            this.aggregateGoalsTeamA=this.getGoalsTeamA()+secondLeg.getGoalsTeamB();
            this.aggregateGoalsTeamB=this.getGoalsTeamB()+secondLeg.getGoalsTeamA();

            if(aggregateGoalsTeamA>aggregateGoalsTeamB) {
                playoffWinner=this.getTeamA();
            } else if (aggregateGoalsTeamA<aggregateGoalsTeamB) {
                playoffWinner=this.getTeamB();
            } else {
                //so số bàn thắng ở sân khách
                int awayGoalsTeamA=secondLeg.getGoalsTeamB();
                int awayGoalsTeamB=this.getGoalsTeamB();
                if(awayGoalsTeamA>awayGoalsTeamB) {
                    playoffWinner=this.getTeamA();
                } else if(awayGoalsTeamA<awayGoalsTeamB) {
                    playoffWinner=this.getTeamB();
                } else {
                    // Chọn ngẫu nhiên nếu vẫn hòa
                    playoffWinner = Math.random() < 0.5 ? this.getTeamA() : this.getTeamB();
                }
            }
        }
    }

    //kết quả vòng play-off
    public String getPlayoffSummary() {
        if(playoffWinner==null) {
            return "Play-off chua quyet dinh doi thang!";
        }
        String region=playoffWinner==getTeamA()?regionTeamA: regionTeamB;
        return String.format("Doi %s ( %s) thang Play-off voi tong ti so %d-%d.",
        playoffWinner.getName(), region, aggregateGoalsTeamA, aggregateGoalsTeamB);
    }
}
