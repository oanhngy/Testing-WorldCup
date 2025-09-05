package com.worldcup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class Tournament_Test {
    private List<Team> createDummyTeams(String prefix, int count) {
        List<Team> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(new Team(prefix + i, prefix, "Coach", "Medical"));
        }
        return list;
    }

    private Player createPlayer(String name, Team team) {
        Player p = new Player(name, 10, "Forward", team.getRegion());
        team.addPlayer(p);
        return p;
    }

    private Goal createGoal(Player player, Team team, int minute, Match match) {
        return new Goal(player, team, minute, match);
    }

    private Match createDummyMatch(Team a, Team b) {
        return new Match(a, b);
    }

    //89. constructor null host- invalid
    @Test
    public void TournamentConstructor_NullHost_BaoLoi() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new Tournament(null));
        assertEquals("Host team khong duoc null!", exception.getMessage());
    }

    //90. constructor-có host- valid
    @Test
    public void TournamentConstructor_ValidHost_KhoiTaoThanhCong() {
        Team host = new Team("VN", "Asia", "Coach", "Medical");
        Tournament tournament = new Tournament(host);
        assertNotNull(tournament);
    }

    //91. đủ đội các khu vực -> tạo playoff
    @Test
    public void ConductPlayoffs_DuSoLuongDoi_Them4TranPlayoff() {
        Tournament tournament = new Tournament(new Team("VN", "Asia", "Coach", "Medical"));
        List<Team> asia = createDummyTeams("Asia", 6);
        List<Team> concacaf = createDummyTeams("Concacaf", 4);
        List<Team> conmebol = createDummyTeams("Conmebol", 5);
        List<Team> oceania = createDummyTeams("Oceania", 1);
        tournament.conductPlayoffs(asia, concacaf, conmebol, oceania);
        assertEquals(4, tournament.getAllMatches().size());
    }

    //92. Châu Á thiếu đội(<6) - invalid
    @Test
    public void ConductPlayoffs_KhoiDuAsia_ThrowException() {
        Tournament tournament = new Tournament(new Team("VN", "Asia", "Coach", "Medical"));
        List<Team> asia = createDummyTeams("Asia", 5); //thiếu
        List<Team> concacaf = createDummyTeams("Concacaf", 4);
        List<Team> conmebol = createDummyTeams("Conmebol", 5);
        List<Team> oceania = createDummyTeams("Oceania", 1);
        assertThrows(IllegalArgumentException.class,
            () -> tournament.conductPlayoffs(asia, concacaf, conmebol, oceania));
    }

    //93. Concacaf thiếu(<4) - invalid
    @Test
    public void ConductPlayoffs_KhoiDuConcacaf_ThrowException() {
        Tournament tournament = new Tournament(new Team("VN", "Asia", "Coach", "Medical"));
        List<Team> asia = createDummyTeams("Asia", 6);
        List<Team> concacaf = createDummyTeams("Concacaf", 3); //thiếu
        List<Team> conmebol = createDummyTeams("Conmebol", 5);
        List<Team> oceania = createDummyTeams("Oceania", 1);
        assertThrows(IllegalArgumentException.class,
            () -> tournament.conductPlayoffs(asia, concacaf, conmebol, oceania));
    }

    //94. Conmebol thiếu(<5) - invalid
    @Test
    public void ConductPlayoffs_KhoiDuConmebol_ThrowException() {
        Tournament tournament = new Tournament(new Team("VN", "Asia", "Coach", "Medical"));
        List<Team> asia = createDummyTeams("Asia", 6);
        List<Team> concacaf = createDummyTeams("Concacaf", 4);
        List<Team> conmebol = createDummyTeams("Conmebol", 4); //thiếu
        List<Team> oceania = createDummyTeams("Oceania", 1);
        assertThrows(IllegalArgumentException.class,
            () -> tournament.conductPlayoffs(asia, concacaf, conmebol, oceania));
    }

    //95. Oceania thiếu(<1) - invalid
    @Test
    public void ConductPlayoffs_KhoiDuOceania_ThrowException() {
        Tournament tournament = new Tournament(new Team("VN", "Asia", "Coach", "Medical"));
        List<Team> asia = createDummyTeams("Asia", 6);
        List<Team> concacaf = createDummyTeams("Concacaf", 4);
        List<Team> conmebol = createDummyTeams("Conmebol", 5);
        List<Team> oceania = createDummyTeams("Oceania", 0); //thiếu
        assertThrows(IllegalArgumentException.class,
            () -> tournament.conductPlayoffs(asia, concacaf, conmebol, oceania));
    }


    //96. đủ đội(32 preQualified+ 1 host) -> chia 8 bảng
    @Test
    public void AssignGroups_Du32Doi_ChiaDu8Bang() {
        Team host = new Team("VN", "Asia", "Coach", "Medical");
        Tournament tournament = new Tournament(host);
        List<Team> list = createDummyTeams("Other", 31);
        tournament.assignGroups(list);
        assertEquals(8, tournament.getGroups().size());
        for (Group g : tournament.getGroups()) {
            assertEquals(4, g.getTeams().size());
        }
    }

    //97. 31 đội, k có host- invalid
    @Test
    public void AssignGroups_Du31Doi_KhongCoHost_ThrowException() {
        Tournament tournament = new Tournament(new Team("VN", "Asia", "Coach", "Medical"));
        List<Team> list = createDummyTeams("Other", 30);
        assertThrows(IllegalArgumentException.class,
            () -> tournament.assignGroups(list));
    }

    //98. <31 đội, có host- invalid
    @Test
    public void AssignGroups_KhongDu31Doi_CoHost_ThrowException() {
        Team host = new Team("VN", "Asia", "Coach", "Medical");
        Tournament tournament = new Tournament(host);
        List<Team> teams = createDummyTeams("Other", 30);
        assertThrows(IllegalArgumentException.class,
            () -> tournament.assignGroups(teams));
    }

    //99. đủ đội, không trùng tên -> valid
    @Test
    public void AssignGroups_Du32Doi_CacDoiKhongTrungTen_Valid() {
        Team host = new Team("VN", "Asia", "Coach", "Medical");
        Tournament tournament = new Tournament(host);
        List<Team> teams = createDummyTeams("Team", 31);
        tournament.assignGroups(teams);

        Set<String> groupNames = new HashSet<>();
        for (Group g : tournament.getGroups()) {
            assertTrue(groupNames.add(g.getGroupName()));
        }
    }

    //100. host team đã nằm trong preQualified- invalid
    @Test
    public void AssignGroups_Du32Doi_TrungHostTeam_ThrowException() {
        Team host = new Team("VN", "Asia", "Coach", "Medical");
        Tournament tournament = new Tournament(host);
        List<Team> teams = createDummyTeams("Team", 30);
        teams.add(host); // trùng host

        assertThrows(IllegalArgumentException.class,
            () -> tournament.assignGroups(teams));
    }

    //101. không có bàn thắng -> trả về null
    @Test
    public void GetTopScorers_KhongCoBanThang_TraVeDanhSachRong() {
        Tournament t = new Tournament(new Team("VN", "Asia", "Coach", "Medical"));
        List<Player> top = t.getTopScorers();
        assertTrue(top.isEmpty());
    }

    //102. 1 cầu thủ ghi bàn -> trả về đúng cầu thủ
    @Test
    public void GetTopScorers_1CauThuGhi1Ban_TraVeDungCauThuDo() {
        Tournament t = new Tournament(new Team("VN", "Asia", "Coach", "Medical"));
        Team team = new Team("TeamA", "Asia", "C", "M");
        Match match = new Match(team, team, false, false);
        Player p1 = createPlayer("Player1", team);
        Goal g = createGoal(p1, team, 10, match);
        t.addGoal(g);
        List<Player> top = t.getTopScorers();
        assertEquals(1, top.size());
        assertEquals(p1, top.get(0));
    }

    //103. 1 cầu thủ ghi nhiều bàn -> trả về đúng cầu thủ
    @Test
    public void GetTopScorers_1CauThuGhiNhieuBan_TraVeDungCauThuDo() {
        Tournament t = new Tournament(new Team("VN", "Asia", "Coach", "Medical"));
        Player p = new Player("Ronaldo", 7, "FW", "POR");
        Team team = new Team("POR", "Europe", "Coach", "Medical");
        Match match = createDummyMatch(team, team);
        t.addGoal(createGoal(p, team, 10, match));
        t.addGoal(createGoal(p, team, 20, match));
        t.addGoal(createGoal(p, team, 30, match));
        List<Player> top = t.getTopScorers();
        assertEquals(1, top.size());
        assertEquals("Ronaldo", top.get(0).getName());
    }

    //104. nhiều cầu thủ-nhiều bàn -> trả về dsach các cầu thủ
    @Test
    public void GetTopScorers_NhieuCauThuGhiNhieuBan_TraVeTatCaNguoiDongDiem() {
        Tournament t = new Tournament(new Team("VN", "Asia", "Coach", "Medical"));
        Player p1 = new Player("Mbappe", 10, "FW", "FRA");
        Player p2 = new Player("Neymar", 11, "FW", "BRA");
        Team team = new Team("TeamX", "World", "Coach", "Medical");
        Match match = createDummyMatch(team, team);
        t.addGoal(createGoal(p1, team, 5, match));
        t.addGoal(createGoal(p2, team, 10, match));
        List<Player> top = t.getTopScorers();
        assertTrue(top.contains(p1));
        assertTrue(top.contains(p2));
    }

    //105. nhiều cầu thủ ghi bàn-1 nhiều highest -> trả về đúng
    @Test
    public void GetTopScorers_MotNguoiNhieuBanHon_TraVeDungNguoiDuyNhat() {
        Tournament t = new Tournament(new Team("VN", "Asia", "Coach", "Medical"));
        Player p1 = new Player("Kane", 9, "FW", "ENG");
        Player p2 = new Player("Son", 7, "FW", "KOR");
        Team team = new Team("World", "Earth", "Coach", "Medical");
        Match match = createDummyMatch(team, team);
        t.addGoal(createGoal(p1, team, 8, match));
        t.addGoal(createGoal(p1, team, 16, match));
        t.addGoal(createGoal(p2, team, 25, match));
        List<Player> top = t.getTopScorers();
        assertEquals(1, top.size());
        assertEquals(p1, top.get(0));
    }

    //106. null bàn thắng- vẫn valid
    @Test
    public void GetTopScorers_ThemGoalNull_KhongLoi() {
        Tournament t = new Tournament(new Team("VN", "Asia", "Coach", "Medical"));
        t.addGoal(null);
        assertTrue(t.getTopScorers().isEmpty());
    }

    //107. player=null- invalid
    @Test
    public void GetTopScorers_ThemBanThangChoCauThuNull_ThrowException() {
        Tournament t = new Tournament(new Team("VN", "Asia", "Coach", "Medical"));
        Team team = new Team("Unknown", "Nowhere", "Coach", "Medical");
        Match match = createDummyMatch(team, team);
        assertThrows(IllegalArgumentException.class, () -> {
            Goal goal = new Goal(null, team, 15, match);
            t.addGoal(goal);
        });
    }

    //108. bàn thắng cùng phút, khác trận -> tính đúng tổng
    @Test
    public void GetTopScorers_GoalsCoCungMinuteKhacMatch_KhongAnhHuongKetQua() {
        Tournament t = new Tournament(new Team("VN", "Asia", "Coach", "Medical"));
        Player p = new Player("Mane", 10, "FW", "SEN");
        Team team = new Team("Senegal", "Africa", "Coach", "Medical");
        Match m1 = createDummyMatch(team, team);
        Match m2 = createDummyMatch(team, team);
        t.addGoal(createGoal(p, team, 10, m1));
        t.addGoal(createGoal(p, team, 10, m2));
        List<Player> top = t.getTopScorers();
        assertEquals(1, top.size());
        assertEquals(p, top.get(0));
    }

}
