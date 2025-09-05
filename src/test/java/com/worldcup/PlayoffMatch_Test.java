package com.worldcup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class PlayoffMatch_Test {
    //tạo team hợp lệ
    private Team createTeam(String name, String region) {
        Team team=new Team(name, region, "Coach","MEdical");
        for(int i=1; i<=11; i++) {
            team.addPlayer(new Player(name+i,i,"MF",region));
        }
        return team;
    }
    //55. khởi tạo hợp lệ
    @Test
    public void PlayoffMatchConstructor_HopLe_KhoiTaoThanhCong() {
        Team teamA = createTeam("TeamA", "Asia");
        Team teamB = createTeam("TeamB", "Oceania");
        PlayoffMatch match = new PlayoffMatch(teamA, teamB, true, "Asia", "Oceania");
        assertNotNull(match);
        assertEquals(teamA, match.getTeamA());
        assertEquals(teamB, match.getTeamB());
        assertTrue(match.isFirstLeg());
    }

    //56. khởi tạo teamA null
    @Test
    public void PlayoffMatchConstructor_TeamANull_ThrowException() {
        Team teamB = createTeam("TeamB", "Oceania");
        assertThrows(IllegalArgumentException.class, () -> {
            new PlayoffMatch(null, teamB, true, "Asia", "Oceania");
        });
    }

    //57. khởi tạo teamB null
    @Test
    public void PlayoffMatchConstructor_TeamBNull_ThrowException() {
        Team teamA = createTeam("TeamA", "Asia");
        assertThrows(IllegalArgumentException.class, () -> {
            new PlayoffMatch(teamA, null, true, "Asia", "Oceania");
        });
    }
    
    //58. lượt về hợp lệ
    @Test
    public void SetSecondLeg_TruyenHopLe_LuuThanhCong() {
        Team teamA = createTeam("TeamA", "Asia");
        Team teamB = createTeam("TeamB", "Oceania");
        PlayoffMatch firstLeg = new PlayoffMatch(teamA, teamB, true, "Asia", "Oceania");
        PlayoffMatch secondLeg = new PlayoffMatch(teamB, teamA, false, "Oceania", "Asia");
        firstLeg.setSecondLeg(secondLeg);
        assertEquals(secondLeg, firstLeg.getSecondLeg());
    }

    //59. SetSecondLeg_ChuaGan_TraVeNull
    @Test
    public void SetSecondLeg_ChuaGan_TraVeNull() {
        Team teamA = createTeam("TeamA", "Asia");
        Team teamB = createTeam("TeamB", "Oceania");
        PlayoffMatch match = new PlayoffMatch(teamA, teamB, true, "Asia", "Oceania");
        assertNull(match.getSecondLeg());
    }

    //60. SetSecondLeg_GanChinhNo_ThrowExceptionOrIgnore
    @Test
    public void SetSecondLeg_GanChinhNo_ThrowExceptionOrIgnore() {
        Team teamA = createTeam("TeamA", "Asia");
        Team teamB = createTeam("TeamB", "Oceania");
        PlayoffMatch match = new PlayoffMatch(teamA, teamB, true, "Asia", "Oceania");
        match.setSecondLeg(match);
        assertEquals(match, match.getSecondLeg());
    }
    //61. IsFirstLeg_True_TraVeTrue
    @Test
    public void IsFirstLeg_True_TraVeTrue() {
        Team teamA = createTeam("TeamA", "Asia");
        Team teamB = createTeam("TeamB", "Oceania");
        PlayoffMatch match = new PlayoffMatch(teamA, teamB, true, "Asia", "Oceania");
        assertTrue(match.isFirstLeg());
    }

    //62. IsFirstLeg_False_TraVeFalse
    @Test
    public void IsFirstLeg_False_TraVeFalse() {
        Team teamA = createTeam("TeamA", "Asia");
        Team teamB = createTeam("TeamB", "Oceania");
        PlayoffMatch match = new PlayoffMatch(teamA, teamB, false, "Asia", "Oceania");
        assertFalse(match.isFirstLeg());
    }
    //63. Tổng 2 trận, team A thắng
    @Test
    public void DetermineWinner_TongTeamAThang_TeamAWinner() {
        Team teamA = createTeam("VN", "Asia");
        Team teamB = createTeam("USA", "NA");
        PlayoffMatch firstLeg = new PlayoffMatch(teamA, teamB, true, "Asia", "NA");
        PlayoffMatch secondLeg = new PlayoffMatch(teamB, teamA, false, "NA", "Asia");
        firstLeg.setGoalsTeamA(2);
        firstLeg.setGoalsTeamB(1);
        firstLeg.setIsFinished(true);
        secondLeg.setGoalsTeamA(0);
        secondLeg.setGoalsTeamB(1);
        secondLeg.setIsFinished(true);
        firstLeg.setSecondLeg(secondLeg);
        firstLeg.determinePlayoffWinner();
        assertEquals("VN", firstLeg.getPlayoffWinner().getName());
    }

    //64. Tong 2 tran, team B thang
    @Test
    public void DetermineWinner_TongTeamBThang_TeamBWinner() {
        Team teamA = createTeam("VN", "Asia");
        Team teamB = createTeam("USA", "NA");
        PlayoffMatch firstLeg = new PlayoffMatch(teamA, teamB, true, "Asia", "NA");
        PlayoffMatch secondLeg = new PlayoffMatch(teamB, teamA, false, "NA", "Asia");
        firstLeg.setGoalsTeamA(0);
        firstLeg.setGoalsTeamB(1);
        firstLeg.setIsFinished(true);
        secondLeg.setGoalsTeamA(2);
        secondLeg.setGoalsTeamB(1);
        secondLeg.setIsFinished(true);
        firstLeg.setSecondLeg(secondLeg);
        firstLeg.determinePlayoffWinner();
        assertEquals("USA", firstLeg.getPlayoffWinner().getName());
    }

    //65. Tong 2 tran hoa, teamA thắng nhờ số bàn thắng sân khách
    @Test
    public void DetermineWinner_Hoa_TeamAThangBangBanSanKhach() {
        Team teamA = createTeam("VN", "Asia");
        Team teamB = createTeam("USA", "NA");
        PlayoffMatch firstLeg = new PlayoffMatch(teamA, teamB, true, "Asia", "NA");
        PlayoffMatch secondLeg = new PlayoffMatch(teamB, teamA, false, "NA", "Asia");
        firstLeg.setGoalsTeamA(1);
        firstLeg.setGoalsTeamB(0);
        firstLeg.setIsFinished(true);
        secondLeg.setGoalsTeamA(2);
        secondLeg.setGoalsTeamB(1);
        secondLeg.setIsFinished(true);
        firstLeg.setSecondLeg(secondLeg);
        firstLeg.determinePlayoffWinner();
        assertEquals("VN", firstLeg.getPlayoffWinner().getName());
    }
    //66. Tong 2 tran hoa, teamB thắng nhờ số bàn thắng sân khách
    @Test
public void DetermineWinner_Hoa_TeamBThangBangBanSanKhach() {
    Team teamA = createTeam("VN", "Asia");
    Team teamB = createTeam("USA", "NA");
    PlayoffMatch firstLeg = new PlayoffMatch(teamA, teamB, true, "Asia", "NA");
    PlayoffMatch secondLeg = new PlayoffMatch(teamB, teamA, false, "NA", "Asia");
    firstLeg.setGoalsTeamA(2);
    firstLeg.setGoalsTeamB(1);
    firstLeg.setIsFinished(true);
    secondLeg.setGoalsTeamA(1); 
    secondLeg.setGoalsTeamB(0); 
    secondLeg.setIsFinished(true);
    firstLeg.setSecondLeg(secondLeg);
    firstLeg.determinePlayoffWinner();
    assertEquals("USA", firstLeg.getPlayoffWinner().getName());
}
    //67. Tổng 2 trận hòa, số bàn thắng sân khách cũng hòa -> random
    @Test
    public void DetermineWinner_HoaHoanToan_RandomKetQua() {
        Team teamA = createTeam("VN", "Asia");
        Team teamB = createTeam("USA", "NA");
        PlayoffMatch firstLeg = new PlayoffMatch(teamA, teamB, true, "Asia", "NA");
        PlayoffMatch secondLeg = new PlayoffMatch(teamB, teamA, false, "NA", "Asia");
        firstLeg.setGoalsTeamA(1);
        firstLeg.setGoalsTeamB(0);
        firstLeg.setIsFinished(true);
        secondLeg.setGoalsTeamA(1);
        secondLeg.setGoalsTeamB(0);
        secondLeg.setIsFinished(true);
        firstLeg.setSecondLeg(secondLeg);
        firstLeg.determinePlayoffWinner();
        String winnerName = firstLeg.getPlayoffWinner().getName();
        assertTrue(winnerName.equals("VN") || winnerName.equals("USA"));
    }

    //68. các trận chưa kết thúc-> k xdinh kết quả
    @Test
    public void DetermineWinner_ChuaDaXong_KhongXacDinh() {
        Team teamA = createTeam("VN", "Asia");
        Team teamB = createTeam("USA", "NA");
        PlayoffMatch firstLeg = new PlayoffMatch(teamA, teamB, true, "Asia", "NA");
        PlayoffMatch secondLeg = new PlayoffMatch(teamB, teamA, false, "NA", "Asia");
        firstLeg.setGoalsTeamA(1);
        firstLeg.setGoalsTeamB(0);
        firstLeg.setIsFinished(true);
        secondLeg.setGoalsTeamA(2);
        secondLeg.setGoalsTeamB(1);
        secondLeg.setIsFinished(false);
        firstLeg.setSecondLeg(secondLeg);
        firstLeg.determinePlayoffWinner();
        assertNull(firstLeg.getPlayoffWinner());
    }

    //69. chưa xdinh đội thắng-> null
    @Test
    public void GetPlayoffWinner_ChuaXacDinh_KetQuaNull() {
        Team teamA = createTeam("VN", "Asia");
        Team teamB = createTeam("USA", "NA");
        PlayoffMatch match = new PlayoffMatch(teamA, teamB, true, "Asia", "NA");
        assertNull(match.getPlayoffWinner());
    }

    //70. đã xdinh đội thắng-> trả về đúng kqua
    @Test
    public void GetPlayoffWinner_DaXacDinh_TraVeDungTeam() {
        Team teamA = createTeam("VN", "Asia");
        Team teamB = createTeam("USA", "NA");
        PlayoffMatch firstLeg = new PlayoffMatch(teamA, teamB, true, "Asia", "NA");
        PlayoffMatch secondLeg = new PlayoffMatch(teamB, teamA, false, "NA", "Asia");
        firstLeg.setGoalsTeamA(2);
        firstLeg.setGoalsTeamB(0);
        firstLeg.setIsFinished(true);
        secondLeg.setGoalsTeamA(1);
        secondLeg.setGoalsTeamB(0);
        secondLeg.setIsFinished(true);
        firstLeg.setSecondLeg(secondLeg);
        firstLeg.determinePlayoffWinner();
        assertEquals("VN", firstLeg.getPlayoffWinner().getName());
    }

    //71. gọi determinePLayoffWinner -> getPlayoffWinner tra ve dung kquua
    @Test
    public void GetPlayoffWinner_SauTinhToan_TraVeDungTeam() {
        Team teamA = createTeam("Argentina", "SA");
        Team teamB = createTeam("New Zealand", "Oceania");
        PlayoffMatch firstLeg = new PlayoffMatch(teamA, teamB, true, "SA", "Oceania");
        PlayoffMatch secondLeg = new PlayoffMatch(teamB, teamA, false, "Oceania", "SA");
        firstLeg.setGoalsTeamA(1);
        firstLeg.setGoalsTeamB(0);
        firstLeg.setIsFinished(true);
        secondLeg.setGoalsTeamA(0);
        secondLeg.setGoalsTeamB(1);
        secondLeg.setIsFinished(true);
        firstLeg.setSecondLeg(secondLeg);
        firstLeg.determinePlayoffWinner();
        Team winner = firstLeg.getPlayoffWinner();
        assertNotNull(winner);
        assertEquals("Argentina", winner.getName());
    }
}
