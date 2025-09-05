package com.worldcup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class Match_Test {

    private Team createTeam(String name) {
        Team team = new Team(name, "Asia", "Coach", "Medical");
        for (int i = 1; i <= 11; i++) {
            team.addPlayer(new Player(name + i, i, "MF", name));
        }
        return team;
    }

    //27. TeamA ghi bàn
    @Test
    public void ScoreGoal_PlayerFromTeamA_TangSoBanThangTeamA() {
        Player player = new Player("A", 9, "FW", "VN");
        Team teamA = new Team("VN", "Asia", "Coach", "Medical");
        Team teamB = new Team("JP", "Asia", "Coach", "Medical");
        teamA.addPlayer(player);
        Match match = new Match(teamA, teamB);
        match.scoreGoal(player);
        assertEquals(1, match.getGoalsTeamA());
        assertTrue(match.getGoalScorers().contains(player));
    }

    //28. cầu thủ k thuộc 2 đội ghi bàn-invalid
    @Test
    public void ScoreGoal_PlayerNotInMatch_ThrowException() {
        Player player = new Player("A", 9, "FW", "US");
        Team teamA = new Team("VN", "Asia", "Coach", "Medical");
        Team teamB = new Team("JP", "Asia", "Coach", "Medical");
        Match match = new Match(teamA, teamB);

        assertThrows(IllegalArgumentException.class, () -> match.scoreGoal(player));
    }

    //29. 2 thẻ vàng-> loại
    @Test
    public void GiveYellowCard_PlayerNhan2The_BiLoai() {
        Player player = new Player("A", 10, "MF", "VN");
        Team teamA = new Team("VN", "Asia", "Coach", "Medical");
        Team teamB = new Team("JP", "Asia", "Coach", "Medical");
        teamA.addPlayer(player);

        Match match = new Match(teamA, teamB);
        match.giveYellowCard(player);
        match.giveYellowCard(player);

        assertTrue(match.isDisqualified(player));
    }

    //30. 1 thẻ đỏ-> loại
    @Test
    public void GiveRedCard_PlayerNhan1TheDo_BiLoai() {
        Player player = new Player("A", 10, "MF", "VN");
        Team teamA = new Team("VN", "Asia", "Coach", "Medical");
        Team teamB = new Team("JP", "Asia", "Coach", "Medical");
        teamA.addPlayer(player);

        Match match = new Match(teamA, teamB);
        match.giveRedCard(player);

        assertTrue(match.isDisqualified(player));
    }

    //31. check CountAvailablePlayers
    @Test
    public void CountAvailablePlayers_KhongBiLoai_DuocThiDau() {
        Player player1 = new Player("A", 1, "GK", "VN");
        Player player2 = new Player("B", 2, "DF", "VN");
        Team team = new Team("VN", "Asia", "Coach", "Medical");
        team.addPlayer(player1);
        team.addPlayer(player2);

        Match match = new Match(team, new Team("JP", "Asia", "Coach", "Medical"));
        match.giveRedCard(player1);

        int available = match.countAvailablePlayers(team);
        assertEquals(1, available);
    }

    //32. xử thua+kết thúc trận nếu có đội <7 cầu thủ
    @Test
    public void CheckForAutoForfeit_TeamACon6Nguoi_Thua030() {
        Team teamA = new Team("VN", "Asia", "Coach", "Medical");
        Team teamB = new Team("JP", "Asia", "Coach", "Medical");
        for (int i = 1; i <= 6; i++)
            teamA.addPlayer(new Player("P" + i, i, "MF", "VN"));
        for (int i = 1; i <= 11; i++)
            teamB.addPlayer(new Player("P" + i, i, "MF", "JP"));

        Match match = new Match(teamA, teamB);
        match.setKnockout(false);
        match.finishMatch(); 
        assertEquals(0, match.getGoalsTeamA());
        assertEquals(3, match.getGoalsTeamB());
        assertTrue(match.isFinished());
    }

    //33. Thay 1 người/lần- valid
    @Test
    public void Substitute_Thay1CauThu1Lan_Valid() {
        Team team = createTeam("VN");
        team.addPlayer(new Player("VN12", 12, "MF", "VN"));
        Match match = new Match(team, createTeam("JP"));
        Player out = team.getPlayers().get(0);
        Player in = team.getPlayers().get(11);
        boolean result = match.substitute(out, in, team);
        assertTrue(result);
    }

    //34. Thay 0 người/lần- valid
    @Test
    public void Substitute_Thay0CauThu1Lan_Valid() {
        Team team = createTeam("VN");
        Match match = new Match(team, createTeam("JP"));
        assertEquals(11, team.getPlayers().size());
    }

    //35. Thay 3 người/lần- valid
    @Test
    public void Substitute_Thay3CauThu1Lan_Valid() {
        Team team = createTeam("VN");
        team.addPlayer(new Player("VN12", 12, "MF", "VN"));
        team.addPlayer(new Player("VN13", 13, "MF", "VN"));
        team.addPlayer(new Player("VN14", 14, "MF", "VN"));
        Match match = new Match(team, createTeam("JP"));
        boolean r1 = match.substitute(team.getPlayers().get(0), team.getPlayers().get(11), team);
        boolean r2 = match.substitute(team.getPlayers().get(1), team.getPlayers().get(12), team);
        boolean r3 = match.substitute(team.getPlayers().get(2), team.getPlayers().get(13), team);
        assertTrue(r1 && r2 && r3);
    }

    //36. Thay >3 cầu thủ/lần- invalid
    @Test
    public void Substitute_Thay4CauThu1Lan_Invalid() {
        Team team = createTeam("VN");
        team.addPlayer(new Player("VN12", 12, "MF", "VN"));
        team.addPlayer(new Player("VN13", 13, "MF", "VN"));
        team.addPlayer(new Player("VN14", 14, "MF", "VN"));
        team.addPlayer(new Player("VN15", 15, "MF", "VN"));
        Match match = new Match(team, createTeam("JP"));
        match.substitute(team.getPlayers().get(0), team.getPlayers().get(11), team);
        match.substitute(team.getPlayers().get(1), team.getPlayers().get(12), team);
        match.substitute(team.getPlayers().get(2), team.getPlayers().get(13), team);
        boolean r4 = match.substitute(team.getPlayers().get(3), team.getPlayers().get(14), team);
        assertFalse(r4); 
    }

    //37. Thay 1 cầu thủ/ lần, thay >3 lần- invalid
    @Test
    public void Substitute_Thay4Luot_Invalid() {
        Team team = createTeam("VN");
        for (int i = 12; i <= 15; i++) {
            team.addPlayer(new Player("VN" + i, i, "MF", "VN"));
        }
        Match match = new Match(team, createTeam("JP"));
        match.substitute(team.getPlayers().get(0), team.getPlayers().get(11), team);
        match.substitute(team.getPlayers().get(1), team.getPlayers().get(12), team);
        match.substitute(team.getPlayers().get(2), team.getPlayers().get(13), team);
        boolean result = match.substitute(team.getPlayers().get(3), team.getPlayers().get(14), team);
        assertFalse(result);
    }

    //38. Thay=Cầu thủ không thuộc team- invalid
    @Test
    public void Substitute_ThayCauThuNgoaiTeam_Invalid() {
        Team team = createTeam("VN");
        Team otherTeam = createTeam("JP");
        Player playerOut = new Player("OUT", 9, "FW", "JP");
        Player playerIn = new Player("IN", 12, "MF", "VN");
        team.addPlayer(playerIn);
        Match match = new Match(team, otherTeam);
        boolean result = match.substitute(playerOut, playerIn, team);
        assertFalse(result);
    }

    //39. check GetResult, chuua xong trận
    @Test
    public void GetResult_ChuaKetThuc_ReturnThongBao() {
        Match match = new Match(new Team("VN", "Asia", "Coach", "Medical"), new Team("JP", "Asia", "Coach", "Medical"));
        String result = match.getResult();
        assertEquals("Tran dau chua ket thuc.", result);
    }

    //40. check GetResult-cho VN thắng vòng bảng
    @Test
    public void GetResult_VongBang_VNThang() {
        Team teamA = new Team("VN", "Asia", "Coach", "Medical");
        Team teamB = new Team("JP", "Asia", "Coach", "Medical");
        Match match = new Match(teamA, teamB);
        match.setIsFinished(true);
        match.setGoalsTeamA(2);
        match.setGoalsTeamB(1);

        String result = match.getResult();
        assertEquals("VN thang (vong bang)!!!", result);
    }

    //41. vòng bảng, thua, điểm giữ nguyên
    @Test
    public void VongBang_Thua_DiemGiuNguyen() {
        Team teamA = new Team("VN", "Asia", "Coach", "Medical");
        Team teamB = new Team("JP", "Asia", "Coach", "Medical");
        for (int i = 1; i <= 11; i++) {
            teamA.addPlayer(new Player("VN" + i, i, "MF", "VN"));
            teamB.addPlayer(new Player("JP" + i, i, "MF", "JP"));
        }

        Match match = new Match(teamA, teamB);
        match.scoreGoal(new Player("JP9", 9, "MF", "JP"));
        match.scoreGoal(new Player("JP10", 10, "MF", "JP"));
        match.finishMatch();
        assertEquals(0, teamA.getPoints());
        assertEquals(3, teamB.getPoints());
    }

    //42. vòng bảng, hòa -> 2 đội +1
    @Test
    public void VongBang_Hoa_MoiTeamCong1Diem() {
        Team teamA = new Team("VN", "Asia", "Coach", "Medical");
        Team teamB = new Team("JP", "Asia", "Coach", "Medical");
        for (int i = 1; i <= 11; i++) {
            teamA.addPlayer(new Player("VN" + i, i, "MF", "VN"));
            teamB.addPlayer(new Player("JP" + i, i, "MF", "JP"));
        }

        Match match = new Match(teamA, teamB);
        match.scoreGoal(new Player("VN9", 9, "MF", "VN"));
        match.scoreGoal(new Player("JP10", 10, "MF", "JP"));
        match.finishMatch();

        assertEquals(1, teamA.getPoints());
        assertEquals(1, teamB.getPoints());
    }


    //43. vòng knock-out, thắng
    @Test
    public void Knockout_Thang_TeamAThangDungGetResult() {
        Team teamA = new Team("VN", "Asia", "Coach", "Medical");
        Team teamB = new Team("JP", "Asia", "Coach", "Medical");
        for (int i = 1; i <= 11; i++) {
            teamA.addPlayer(new Player("VN" + i, i, "MF", "VN"));
            teamB.addPlayer(new Player("JP" + i, i, "MF", "JP"));
        }

        Match match = new Match(teamA, teamB, true);
        match.scoreGoal(new Player("VN9", 9, "FW", "VN"));
        match.finishMatch();
        String result = match.getResult();
        assertEquals("VN thang (knock-out)!!!", result);
    }

    //44. vòng knock-out, thua
    @Test
    public void Knockout_Thua_TeamBThangDungGetResult() {
        Team teamA = new Team("VN", "Asia", "Coach", "Medical");
        Team teamB = new Team("JP", "Asia", "Coach", "Medical");
        for (int i = 1; i <= 11; i++) {
            teamA.addPlayer(new Player("VN" + i, i, "MF", "VN"));
            teamB.addPlayer(new Player("JP" + i, i, "MF", "JP"));
        }

        Match match = new Match(teamA, teamB, true);
        match.scoreGoal(new Player("JP10", 10, "FW", "JP"));
        match.finishMatch();
        String result = match.getResult();
        assertEquals("JP thang (knock-out)!!!", result);
    }

    //45. update điểm, thua
    @Test
    public void VongBang_UpdateDiemKhiThua() {
        Team teamA = new Team("VN", "Asia", "Coach", "Medical");
        Team teamB = new Team("JP", "Asia", "Coach", "Medical");
        for (int i = 1; i <= 11; i++) {
            teamA.addPlayer(new Player("VN" + i, i, "MF", "VN"));
            teamB.addPlayer(new Player("JP" + i, i, "MF", "JP"));
        }

        Match match = new Match(teamA, teamB);
        match.scoreGoal(new Player("JP11", 11, "FW", "JP"));
        match.finishMatch();
        assertEquals(0, teamA.getPoints());
        assertEquals(3, teamB.getPoints());
    }

    //46. update điểm, thắng
    @Test
    public void VongBang_UpdateDiemKhiThang() {
        Team teamA = new Team("VN", "Asia", "Coach", "Medical");
        Team teamB = new Team("JP", "Asia", "Coach", "Medical");
        for (int i = 1; i <= 11; i++) {
            teamA.addPlayer(new Player("VN" + i, i, "MF", "VN"));
            teamB.addPlayer(new Player("JP" + i, i, "MF", "JP"));
        }

        Match match = new Match(teamA, teamB);
        match.scoreGoal(new Player("VN9", 9, "FW", "VN"));
        match.finishMatch();
        assertEquals(3, teamA.getPoints());
        assertEquals(0, teamB.getPoints());
    }

    //47. update điểm, hòa
    @Test
    public void VongBang_UpdateDiemKhiHoa() {
        Team teamA = new Team("VN", "Asia", "Coach", "Medical");
        Team teamB = new Team("JP", "Asia", "Coach", "Medical");
        for (int i = 1; i <= 11; i++) {
            teamA.addPlayer(new Player("VN" + i, i, "MF", "VN"));
            teamB.addPlayer(new Player("JP" + i, i, "MF", "JP"));
        }

        Match match = new Match(teamA, teamB); // Vòng bảng
        match.scoreGoal(new Player("VN1", 1, "MF", "VN"));
        match.scoreGoal(new Player("JP1", 1, "MF", "JP"));
        match.finishMatch();
        assertEquals(1, teamA.getPoints());
        assertEquals(1, teamB.getPoints());
    }

    //48. trận knockout hòa -> hiệp phụ 1
    @Test
    public void Knockout_Hoa_ChuyenSangHiepPhu() {
        Team teamA = createTeam("VN");
        Team teamB = createTeam("JP");
        Match match = new Match(teamA, teamB, true);
        match.scoreGoal(new Player("VN9", 9, "FW", "VN"));
        match.scoreGoal(new Player("JP10", 10, "FW", "JP"));
        Team winner = match.finishMatch();
        assertNotNull(winner);
    }

    //49. hiệp phụ 1 end với kết quả 2-1-> có team thắng
    @Test
    public void ExtraTime1_BanThangBac() {
        Team teamA = createTeam("VN");
        Team teamB = createTeam("JP");
        Match match = new Match(teamA, teamB, true);
        match.scoreGoal(new Player("VN9", 9, "FW", "VN"));
        match.scoreGoal(new Player("VN10", 10, "FW", "VN"));
        match.scoreGoal(new Player("JP11", 11, "FW", "JP"));
        Team winner = match.finishMatch();
        assertNotNull(winner);
        assertEquals("VN", winner.getName());
    }

    //50. hiệp phụ 1 hòa -> tiếp hiệp phụ 2
    @Test
    public void ExtraTime1_Hoa_TiepHiep2() {
        Team teamA = createTeam("VN");
        Team teamB = createTeam("JP");
        Match match = new Match(teamA, teamB, true);
        match.scoreGoal(new Player("VN9", 9, "FW", "VN"));
        match.scoreGoal(new Player("JP11", 11, "FW", "JP"));
        Team winner = match.finishMatch();
        assertNotNull(winner);
    }

    //51. hiệp phụ 2 có kết quả
    @Test
    public void ExtraTime2_ThangKetThuc() {
        Team teamA = createTeam("VN");
        Team teamB = createTeam("JP");
        Match match = new Match(teamA, teamB, true);
        match.scoreGoal(new Player("VN9", 9, "FW", "VN"));
        match.scoreGoal(new Player("JP11", 11, "FW", "JP"));
        match.scoreGoal(new Player("VN10", 10, "FW", "VN"));
        Team winner = match.finishMatch();
        assertNotNull(winner);
        assertEquals("VN", winner.getName());
    }

    //52. hiệp phụ 2 hòa-> penalty-> teamA thắng
    @Test
    public void PenaltySauExtraTime_Hoa_CoTeamThang() {
        Team teamA = createTeam("VN");
        Team teamB = createTeam("JP");
        Match match = new Match(teamA, teamB, true);
        match.scoreGoal(new Player("VN1", 1, "MF", "VN"));
        match.scoreGoal(new Player("VN2", 2, "MF", "VN"));
        match.scoreGoal(new Player("VN3", 3, "MF", "VN"));
        match.scoreGoal(new Player("JP1", 1, "MF", "JP"));
        match.scoreGoal(new Player("JP2", 2, "MF", "JP"));
        match.scoreGoal(new Player("JP3", 3, "MF", "JP"));

        Team winner = match.finishMatch();
        assertNotNull(winner);
        assertTrue(winner.getName().equals("VN") || winner.getName().equals("JP"));
    }

    //53. check penalty only
    @Test
    public void Penalty_KetQuaThang() {
        Team teamA = createTeam("VN");
        Team teamB = createTeam("JP");
        Match match = new Match(teamA, teamB, true);
        match.scoreGoal(new Player("VN1", 1, "MF", "VN"));
        match.scoreGoal(new Player("VN2", 2, "MF", "VN"));
        match.scoreGoal(new Player("VN3", 3, "MF", "VN"));
        match.scoreGoal(new Player("VN4", 4, "MF", "VN"));
        match.scoreGoal(new Player("VN5", 5, "MF", "VN"));
        match.scoreGoal(new Player("JP1", 1, "MF", "JP"));
        match.scoreGoal(new Player("JP2", 2, "MF", "JP"));
        match.scoreGoal(new Player("JP3", 3, "MF", "JP"));
        match.scoreGoal(new Player("JP4", 4, "MF", "JP"));

        Team winner = match.finishMatch();
        assertNotNull(winner);
        assertEquals("VN", winner.getName());
    }

    //54. penalty hòa, tiếp tục penalty cho tới khi win
    @Test
    public void Penalty_Hoa_TiepTucChoDenThang() {
        Team teamA = createTeam("VN");
        Team teamB = createTeam("JP");
        Match match = new Match(teamA, teamB, true);
        for (int i = 0; i < 5; i++) {
            match.scoreGoal(new Player("VN" + i, i + 1, "MF", "VN"));
            match.scoreGoal(new Player("JP" + i, i + 1, "MF", "JP"));
        }
        match.scoreGoal(new Player("JP6", 6, "MF", "JP"));
        Team winner = match.finishMatch();
        assertNotNull(winner);
        assertEquals("JP", winner.getName());
    }
}
