package com.worldcup;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class KnockoutStageManagement_Test {
    //========== HELPERS ==========
    private Team dummyTeam(String name) {
        Team t = mock(Team.class, withSettings()
                .defaultAnswer(Answers.RETURNS_DEFAULTS)
                .lenient());
        when(t.getName()).thenReturn(name);
        return t;
    }

    //group đúng
    private Group dummyGroup(String groupName, String top1, String top2) {
        Group g = mock(Group.class, withSettings()
                .defaultAnswer(Answers.RETURNS_DEFAULTS)
                .lenient());
        when(g.getGroupName()).thenReturn(groupName);
        Team t1 = dummyTeam(top1);
        Team t2 = dummyTeam(top2);
        java.util.List<Team> top2List = java.util.Arrays.asList(t1, t2);
        when(g.getTop2Teams()).thenReturn(top2List);
        return g;
    }

    //gr sai
    private Group dummyGroupTop1Only(String groupName, String top1) {
        Group g = mock(Group.class, withSettings()
                .defaultAnswer(Answers.RETURNS_DEFAULTS)
                .lenient());
        when(g.getGroupName()).thenReturn(groupName);

        Team t1 = dummyTeam(top1);
        when(g.getTop2Teams()).thenReturn(java.util.Collections.singletonList(t1));
        return g;
    }

    private Tournament dummyTournament(java.util.Collection<Group> groups) {
        Tournament t = mock(Tournament.class, withSettings()
                .defaultAnswer(Answers.RETURNS_DEFAULTS)
                .lenient());
        when(t.getGroups()).thenReturn(new java.util.ArrayList<>(groups));
        return t;
    }

    //match đã hoàn tất
    private Match finishedMatch(Team a, Team b, int ga, int gb) {
        Match m = mock(Match.class, withSettings().defaultAnswer(Answers.RETURNS_DEFAULTS).lenient());
        when(m.getTeamA()).thenReturn(a);
        when(m.getTeamB()).thenReturn(b);
        when(m.getGoalsTeamA()).thenReturn(ga);
        when(m.getGoalsTeamB()).thenReturn(gb);
        when(m.isFinished()).thenReturn(true);
        return m;
    }

    //match chưa
    private Match unfinishedMatch(Team a, Team b) {
        Match m = mock(Match.class, withSettings().defaultAnswer(Answers.RETURNS_DEFAULTS).lenient());
        when(m.getTeamA()).thenReturn(a);
        when(m.getTeamB()).thenReturn(b);
        when(m.isFinished()).thenReturn(false);
        return m;
    }

    //match hoàn tất nhưng HÒA
    private Match drawMatch(Team a, Team b, int score) {
        Match m = mock(Match.class, withSettings().defaultAnswer(Answers.RETURNS_DEFAULTS).lenient());
        when(m.getTeamA()).thenReturn(a);
        when(m.getTeamB()).thenReturn(b);
        when(m.getGoalsTeamA()).thenReturn(score);
        when(m.getGoalsTeamB()).thenReturn(score);
        when(m.isFinished()).thenReturn(true);
        return m;
    }

    @SuppressWarnings("unchecked")
    private <T> T invokePrivateStatic(String methodName, Class<?>[] paramTypes, Object... args) {
        try {
            Method m = KnockoutStageManagement.class.getDeclaredMethod(methodName, paramTypes);
            m.setAccessible(true);
            return (T) m.invoke(null, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException re) throw re;
            throw new RuntimeException(cause);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callEnsureFinished(List<Match> matches, String roundName) {
        invokePrivateStatic("ensureFinished", new Class<?>[]{List.class, String.class}, matches, roundName);
    }

    private Team callWinnerOf(Match m) {
        return invokePrivateStatic("winnerOf", new Class<?>[]{Match.class}, m);
    }

    private Team callLoserOf(Match m) {
        return invokePrivateStatic("loserOf", new Class<?>[]{Match.class}, m);
    }

    //========== HELPERS ==========

    //120. tournament null- invalid
    @Test
    void KnockoutStageManagementConstructor_TournamentNull_ThatBai() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new KnockoutStageManagement(null)
        );
        assertEquals("Tournament khong duoc null!", ex.getMessage());
    }

    //121. constructor- valid
    @Test
    void KnockoutStageManagementConstructor_Tournament_ThanhCong() {
        Tournament t = dummyTournament(List.of());
        assertDoesNotThrow(() -> new KnockoutStageManagement(t));
    }

    //122. BracketInfo mặc định- dsach rỗng
    @Test
    void BracketInfo_GetterMacDinh_DanhSachRong() {
        KnockoutStageManagement km = new KnockoutStageManagement(dummyTournament(List.of()));
        KnockoutStageManagement.BracketInfo b = km.getBracket();

        assertNotNull(b);
        assertEquals(0, b.getRoundOf16().size());
        assertEquals(0, b.getQuarterFinals().size());
        assertEquals(0, b.getSemiFinals().size());
        assertNull(b.getFinalMatch());
    }

    //123. gọi snapshot khi rỗng- valid
    @Test
    void BracketInfo_Snapshot_TrangThaiRong_XuatChuoiKhongLoi() {
        KnockoutStageManagement km = new KnockoutStageManagement(dummyTournament(List.of()));
        String snap = assertDoesNotThrow(() -> km.getBracket().snapshot());
        assertNotNull(snap);
        assertTrue(snap.contains("Round of 16"));
    }

    //124. seedRounfOf16-tournament null- invalid
    @Test
    void SeedRoundOf16_TournamentNull_ThrowException() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> new KnockoutStageManagement(null)
        );
        assertEquals("Tournament khong duoc null!", ex.getMessage());
    }

    //125. 7 nhóm- invalid
    @Test
    void SeedRoundOf16_Co7Nhom_Thieu_ThrowException() {
        List<Group> groups = List.of(
            dummyGroup("A","A1","A2"),
            dummyGroup("B","B1","B2"),
            dummyGroup("C","C1","C2"),
            dummyGroup("D","D1","D2"),
            dummyGroup("E","E1","E2"),
            dummyGroup("F","F1","F2"),
            dummyGroup("G","G1","G2")
        );
        KnockoutStageManagement km = new KnockoutStageManagement(dummyTournament(groups));
        assertThrows(IllegalArgumentException.class, km::seedRoundOf16FromGroups);
    }

    //126. 9 nhóm- invalid
    @Test
    void SeedRoundOf16_Co9Nhom_Thua_ThrowException() {
        List<Group> groups = new ArrayList<>(List.of(
                dummyGroup("A", "A1", "A2"),
                dummyGroup("B", "B1", "B2"),
                dummyGroup("C", "C1", "C2"),
                dummyGroup("D", "D1", "D2"),
                dummyGroup("E", "E1", "E2"),
                dummyGroup("F", "F1", "F2"),
                dummyGroup("G", "G1", "G2"),
                dummyGroup("H", "H1", "H2")
        ));
        groups.add(dummyGroup("X", "X1", "X2")); // thừa
        KnockoutStageManagement km = new KnockoutStageManagement(dummyTournament(groups));
        assertThrows(IllegalArgumentException.class, km::seedRoundOf16FromGroups);
    }

    //127. k đủ bảng-thiếu bảng A- invalid
    @Test
    void SeedRoundOf16_ThieuBangA_ThrowException() {
        List<Group> groups = List.of(
                dummyGroup("B", "B1", "B2"),
                dummyGroup("C", "C1", "C2"),
                dummyGroup("D", "D1", "D2"),
                dummyGroup("E", "E1", "E2"),
                dummyGroup("F", "F1", "F2"),
                dummyGroup("G", "G1", "G2"),
                dummyGroup("H", "H1", "H2"),
                dummyGroup("X", "X1", "X2")
        );
        KnockoutStageManagement km = new KnockoutStageManagement(dummyTournament(groups));
        assertThrows(IllegalArgumentException.class, km::seedRoundOf16FromGroups);
    }

    //128. đủ bảng nhưng trong các bảng bị thiếu đội- invalid
    @Test
    void SeedRoundOf16_BangAChiCo1Doi_ThrowException() {
        Group ga = dummyGroupTop1Only("A", "A1");
        List<Group> groups = List.of(
                ga,
                dummyGroup("B", "B1", "B2"),
                dummyGroup("C", "C1", "C2"),
                dummyGroup("D", "D1", "D2"),
                dummyGroup("E", "E1", "E2"),
                dummyGroup("F", "F1", "F2"),
                dummyGroup("G", "G1", "G2"),
                dummyGroup("H", "H1", "H2")
        );
        KnockoutStageManagement km = new KnockoutStageManagement(dummyTournament(groups));
        assertThrows(IllegalArgumentException.class, km::seedRoundOf16FromGroups);
    }

    //129. đủ 8 bảng- đủ 2 đội/bảng- valid
    @Test
    void SeedRoundOf16_Du8Bang_MoiBangTop2_Tao8TranDungThuTu() {
        List<Group> groups = List.of(
                dummyGroup("A", "A1", "A2"),
                dummyGroup("B", "B1", "B2"),
                dummyGroup("C", "C1", "C2"),
                dummyGroup("D", "D1", "D2"),
                dummyGroup("E", "E1", "E2"),
                dummyGroup("F", "F1", "F2"),
                dummyGroup("G", "G1", "G2"),
                dummyGroup("H", "H1", "H2")
        );
        KnockoutStageManagement km = new KnockoutStageManagement(dummyTournament(groups));
        km.seedRoundOf16FromGroups();
        List<Match> r16 = km.getBracket().getRoundOf16();
        assertEquals(8, r16.size());
        assertEquals("A1", r16.get(0).getTeamA().getName());
        assertEquals("B2", r16.get(0).getTeamB().getName());
        assertEquals("B1", r16.get(1).getTeamA().getName());
        assertEquals("A2", r16.get(1).getTeamB().getName());
        assertEquals("C1", r16.get(2).getTeamA().getName());
        assertEquals("D2", r16.get(2).getTeamB().getName());
        assertEquals("D1", r16.get(3).getTeamA().getName());
        assertEquals("C2", r16.get(3).getTeamB().getName());
        assertEquals("E1", r16.get(4).getTeamA().getName());
        assertEquals("F2", r16.get(4).getTeamB().getName());
        assertEquals("F1", r16.get(5).getTeamA().getName());
        assertEquals("E2", r16.get(5).getTeamB().getName());
        assertEquals("G1", r16.get(6).getTeamA().getName());
        assertEquals("H2", r16.get(6).getTeamB().getName());
        assertEquals("H1", r16.get(7).getTeamA().getName());
        assertEquals("G2", r16.get(7).getTeamB().getName());
    }

    //130. gọi lại sau seed- valid
    @Test
    void SeedRoundOf16_GoiLaiSauDaSeed_ClearVaSeedLaiThanhCong() {
        List<Group> groups = List.of(
                dummyGroup("A", "A1", "A2"),
                dummyGroup("B", "B1", "B2"),
                dummyGroup("C", "C1", "C2"),
                dummyGroup("D", "D1", "D2"),
                dummyGroup("E", "E1", "E2"),
                dummyGroup("F", "F1", "F2"),
                dummyGroup("G", "G1", "G2"),
                dummyGroup("H", "H1", "H2")
        );
        KnockoutStageManagement km = new KnockoutStageManagement(dummyTournament(groups));
        km.seedRoundOf16FromGroups();
        assertEquals(8, km.getBracket().getRoundOf16().size());
        km.seedRoundOf16FromGroups();
        List<Match> r16 = km.getBracket().getRoundOf16();
        assertEquals(8, r16.size());
        assertEquals("A1", r16.get(0).getTeamA().getName());
        assertEquals("B2", r16.get(0).getTeamB().getName());
        assertEquals("H1", r16.get(7).getTeamA().getName());
        assertEquals("G2", r16.get(7).getTeamB().getName());
    }

    //131. chưa seed Round of 16- invalid
    @Test
    void BuildQuarterFinals_ChuaSeedRoundOf16_ThrowException() {
        KnockoutStageManagement km = new KnockoutStageManagement(dummyTournament(List.of()));
        assertThrows(IllegalStateException.class, km::buildQuarterFinals);
    }

    //132. R16 at least 1 trận chưa finish- invalid
    @Test
    void BuildQuarterFinals_R16ChuaHoanTat_ThrowException() {
        KnockoutStageManagement km = new KnockoutStageManagement(dummyTournament(List.of()));
        List<Match> r16 = km.getBracket().getRoundOf16();
        r16.clear();
        r16.add(finishedMatch(dummyTeam("A1"), dummyTeam("B2"), 1, 0));
        r16.add(finishedMatch(dummyTeam("B1"), dummyTeam("A2"), 2, 1));
        r16.add(finishedMatch(dummyTeam("C1"), dummyTeam("D2"), 3, 0));
        r16.add(finishedMatch(dummyTeam("D1"), dummyTeam("C2"), 2, 0));
        r16.add(finishedMatch(dummyTeam("E1"), dummyTeam("F2"), 1, 0));
        r16.add(finishedMatch(dummyTeam("F1"), dummyTeam("E2"), 2, 0));
        r16.add(finishedMatch(dummyTeam("G1"), dummyTeam("H2"), 1, 0));
        r16.add(unfinishedMatch(dummyTeam("H1"), dummyTeam("G2"))); //trận chưa finish
        assertThrows(IllegalStateException.class, km::buildQuarterFinals);
    }

    //133. R16 có trận hòa (knockout k đc hòa)- invalid
    @Test
    void BuildQuarterFinals_R16CoTranHoa_ThrowException() {
        KnockoutStageManagement km = new KnockoutStageManagement(dummyTournament(List.of()));
        List<Match> r16 = km.getBracket().getRoundOf16();
        r16.clear();
        r16.add(finishedMatch(dummyTeam("A1"), dummyTeam("B2"), 1, 0));
        r16.add(finishedMatch(dummyTeam("B1"), dummyTeam("A2"), 2, 1));
        r16.add(drawMatch    (dummyTeam("C1"), dummyTeam("D2"), 1)); //hòa
        r16.add(finishedMatch(dummyTeam("D1"), dummyTeam("C2"), 2, 0));
        r16.add(finishedMatch(dummyTeam("E1"), dummyTeam("F2"), 1, 0));
        r16.add(finishedMatch(dummyTeam("F1"), dummyTeam("E2"), 2, 0));
        r16.add(finishedMatch(dummyTeam("G1"), dummyTeam("H2"), 1, 0));
        r16.add(finishedMatch(dummyTeam("H1"), dummyTeam("G2"), 2, 0));
        assertThrows(IllegalStateException.class, km::buildQuarterFinals);
    }

    //134. R16 đầy đủ- valid
    @Test
    void BuildQuarterFinals_R16DayDuKetQua_Tao4TranDungThuTu() {
        KnockoutStageManagement km = new KnockoutStageManagement(dummyTournament(List.of()));
        List<Match> r16 = km.getBracket().getRoundOf16();
        r16.clear();

        Team A1 = dummyTeam("A1"), B1 = dummyTeam("B1");
        Team C1 = dummyTeam("C1"), D1 = dummyTeam("D1");
        Team E1 = dummyTeam("E1"), F1 = dummyTeam("F1");
        Team G1 = dummyTeam("G1"), H1 = dummyTeam("H1");

        r16.add(finishedMatch(A1, dummyTeam("B2"), 1, 0));
        r16.add(finishedMatch(B1, dummyTeam("A2"), 1, 0));
        r16.add(finishedMatch(C1, dummyTeam("D2"), 1, 0));
        r16.add(finishedMatch(D1, dummyTeam("C2"), 1, 0));
        r16.add(finishedMatch(E1, dummyTeam("F2"), 1, 0));
        r16.add(finishedMatch(F1, dummyTeam("E2"), 1, 0));
        r16.add(finishedMatch(G1, dummyTeam("H2"), 1, 0));
        r16.add(finishedMatch(H1, dummyTeam("G2"), 1, 0));

        km.buildQuarterFinals();
        List<Match> qf = km.getBracket().getQuarterFinals();
        assertEquals(4, qf.size());

        // Q1 = W1–W2
        assertEquals("A1", qf.get(0).getTeamA().getName());
        assertEquals("B1", qf.get(0).getTeamB().getName());
        // Q2 = W3–W4
        assertEquals("C1", qf.get(1).getTeamA().getName());
        assertEquals("D1", qf.get(1).getTeamB().getName());
        // Q3 = W5–W6
        assertEquals("E1", qf.get(2).getTeamA().getName());
        assertEquals("F1", qf.get(2).getTeamB().getName());
        // Q4 = W7–W8
        assertEquals("G1", qf.get(3).getTeamA().getName());
        assertEquals("H1", qf.get(3).getTeamB().getName());
    }

    //135. gọi hàm lần nữa- xuất lại đúng- valid
    @Test
    void BuildQuarterFinals_GoiLaiSauDaBuild_ClearVaBuildLaiThanhCong() {
        KnockoutStageManagement km = new KnockoutStageManagement(dummyTournament(List.of()));
        List<Match> r16 = km.getBracket().getRoundOf16();
        r16.clear();

        Team A1 = dummyTeam("A1"), B1 = dummyTeam("B1");
        Team C1 = dummyTeam("C1"), D1 = dummyTeam("D1");
        Team E1 = dummyTeam("E1"), F1 = dummyTeam("F1");
        Team G1 = dummyTeam("G1"), H1 = dummyTeam("H1");

        r16.add(finishedMatch(A1, dummyTeam("B2"), 2, 0));
        r16.add(finishedMatch(B1, dummyTeam("A2"), 2, 1));
        r16.add(finishedMatch(C1, dummyTeam("D2"), 1, 0));
        r16.add(finishedMatch(D1, dummyTeam("C2"), 3, 0));
        r16.add(finishedMatch(E1, dummyTeam("F2"), 1, 0));
        r16.add(finishedMatch(F1, dummyTeam("E2"), 2, 0));
        r16.add(finishedMatch(G1, dummyTeam("H2"), 1, 0));
        r16.add(finishedMatch(H1, dummyTeam("G2"), 4, 0));

        //build lần 1
        km.buildQuarterFinals();
        assertEquals(4, km.getBracket().getQuarterFinals().size());
        //lần 2-clear và build lại vẫn đúng
        km.buildQuarterFinals();
        List<Match> qf = km.getBracket().getQuarterFinals();
        assertEquals(4, qf.size());
        assertEquals("A1", qf.get(0).getTeamA().getName());
        assertEquals("B1", qf.get(0).getTeamB().getName());
    }



    //136. chưa có dsach QuarterFinal- invalid
    @Test
    void BuildSemiFinals_ChuaCoQuarterFinals_ThrowException() {
        KnockoutStageManagement km = new KnockoutStageManagement(dummyTournament(List.of()));
        assertThrows(IllegalStateException.class, km::buildSemiFinals);
    }


    //137. QuarterFinal at least 1 trận chưa xong- invalid
    @Test
    void BuildSemiFinals_QuarterFinalChuaHoanTat_ThrowException() {
        KnockoutStageManagement km = new KnockoutStageManagement(dummyTournament(List.of()));
        List<Match> qf = km.getBracket().getQuarterFinals();
        qf.clear();
        qf.add(finishedMatch(dummyTeam("W1"), dummyTeam("W2"), 1, 0));
        qf.add(finishedMatch(dummyTeam("W3"), dummyTeam("W4"), 2, 0));
        qf.add(finishedMatch(dummyTeam("W5"), dummyTeam("W6"), 3, 1));
        qf.add(unfinishedMatch(dummyTeam("W7"), dummyTeam("W8"))); // Q4 chưa hoàn tất
        assertThrows(IllegalStateException.class, km::buildSemiFinals);
    }

    //138. QF có trận hòa- invalid
    @Test
    void BuildSemiFinals_QuarterFinalCoTranHoa_ThrowException() {
        KnockoutStageManagement km = new KnockoutStageManagement(dummyTournament(List.of()));
        List<Match> qf = km.getBracket().getQuarterFinals();
        qf.clear();
        qf.add(finishedMatch(dummyTeam("W1"), dummyTeam("W2"), 1, 0));
        qf.add(drawMatch    (dummyTeam("W3"), dummyTeam("W4"), 2)); //Q2 hòa
        qf.add(finishedMatch(dummyTeam("W5"), dummyTeam("W6"), 3, 1));
        qf.add(finishedMatch(dummyTeam("W7"), dummyTeam("W8"), 2, 0));
        assertThrows(IllegalStateException.class, km::buildSemiFinals);
    }

    //139. đầy đủ- VALID
    @Test
    void BuildSemiFinals_QuarterFinalDuKetQua_Tao2TranDungThuTu() {
        KnockoutStageManagement km = new KnockoutStageManagement(dummyTournament(List.of()));
        List<Match> qf = km.getBracket().getQuarterFinals();
        qf.clear();

        Team WQ1 = dummyTeam("WQ1"); //thắng Q1
        Team LQ1 = dummyTeam("LQ1");
        Team WQ2 = dummyTeam("WQ2"); //thắng Q2
        Team LQ2 = dummyTeam("LQ2");
        Team WQ3 = dummyTeam("WQ3"); //thắng Q3
        Team LQ3 = dummyTeam("LQ3");
        Team WQ4 = dummyTeam("WQ4"); //thắng Q4
        Team LQ4 = dummyTeam("LQ4");

        qf.add(finishedMatch(WQ1, LQ1, 2, 0));
        qf.add(finishedMatch(WQ2, LQ2, 1, 0));
        qf.add(finishedMatch(WQ3, LQ3, 3, 1));
        qf.add(finishedMatch(WQ4, LQ4, 2, 1));

        km.buildSemiFinals();
        List<Match> sf = km.getBracket().getSemiFinals();
        assertEquals(2, sf.size());

        // S1 = W(Q1) – W(Q2)
        assertEquals("WQ1", sf.get(0).getTeamA().getName());
        assertEquals("WQ2", sf.get(0).getTeamB().getName());
        // S2 = W(Q3) – W(Q4)
        assertEquals("WQ3", sf.get(1).getTeamA().getName());
        assertEquals("WQ4", sf.get(1).getTeamB().getName());
    }



    //140. dsach SemiFinal chưa có- invalid
    @Test
    void BuildFinal_ChuaCoSemiFinal_ThrowException() {
        KnockoutStageManagement km = new KnockoutStageManagement(dummyTournament(List.of()));
        assertThrows(IllegalStateException.class, km::buildFinal);
    }

    //141. SemiFinal có trận chưa finish- invalid
    @Test
    void BuildFinal_SFChuaHoaTat_ThrowException() {
        KnockoutStageManagement km = new KnockoutStageManagement(dummyTournament(List.of()));
        List<Match> sf = km.getBracket().getSemiFinals();
        sf.clear();
        sf.add(finishedMatch(dummyTeam("S1A"), dummyTeam("S1B"), 1, 0));
        sf.add(unfinishedMatch(dummyTeam("S2A"), dummyTeam("S2B"))); // S2 chưa xong
        assertThrows(IllegalStateException.class, km::buildFinal);
    }

    //142. SemiFinal đầy đủ- valid
    @Test
    void BuildFinal_SFDuKetQua_TaoTranChungKet() {
        KnockoutStageManagement km = new KnockoutStageManagement(dummyTournament(List.of()));
        List<Match> sf = km.getBracket().getSemiFinals();
        sf.clear();
        Team S1W = dummyTeam("S1W"), S1L = dummyTeam("S1L");
        Team S2W = dummyTeam("S2W"), S2L = dummyTeam("S2L");
        sf.add(finishedMatch(S1W, S1L, 2, 0)); // S1 -> S1W
        sf.add(finishedMatch(S2W, S2L, 1, 0)); // S2 -> S2W
        km.buildFinal();
        Match fm = km.getBracket().getFinalMatch();
        assertNotNull(fm, "Final match phải được tạo");
        assertEquals("S1W", fm.getTeamA().getName());
        assertEquals("S2W", fm.getTeamB().getName());
}

    //143. chưa tạo trận Final- invalid
    //  * ResolveMedals_ChuaTaoFinal_ThrowException
    @Test
    void ResolveMedals_ChuaTaoFinal_ThrowException() {
        KnockoutStageManagement km = new KnockoutStageManagement(dummyTournament(List.of()));
        km.getBracket().setFinalMatch(null);
        assertThrows(IllegalStateException.class, km::resolveMedals);
    }

    //144. Final chưa xong- invalid
    @Test
    void ResolveMedals_FinalChuaCoTiSo_ThrowException() {
        KnockoutStageManagement km = new KnockoutStageManagement(dummyTournament(List.of()));
        Match unfinishedFinal = unfinishedMatch(dummyTeam("FA"), dummyTeam("FB"));
        km.getBracket().setFinalMatch(unfinishedFinal);
        assertThrows(IllegalStateException.class, km::resolveMedals);
    }

    //145. Final hòa- invalid
    @Test
    void ResolveMedals_FinalHoa_ThrowException() {
        KnockoutStageManagement km = new KnockoutStageManagement(dummyTournament(List.of()));
        Match drawFinal = drawMatch(dummyTeam("FA"), dummyTeam("FB"), 1);
        km.getBracket().setFinalMatch(drawFinal);
        assertThrows(IllegalStateException.class, km::resolveMedals);
    }

    //146. Final VALID
    @Test
    void ResolveMedals_FinalValid_XuatKetQuaDung() {
        KnockoutStageManagement km = new KnockoutStageManagement(dummyTournament(List.of()));

        List<Match> sf = km.getBracket().getSemiFinals();
        sf.clear();

        Team S1W = dummyTeam("S1W"), S1L = dummyTeam("S1L");
        Team S2W = dummyTeam("S2W"), S2L = dummyTeam("S2L");

        sf.add(finishedMatch(S1W, S1L, 2, 0));
        sf.add(finishedMatch(S2W, S2L, 3, 1));

        km.buildFinal();

        km.getBracket().setFinalMatch(finishedMatch(S1W, S2W, 2, 1));

        assertDoesNotThrow(km::resolveMedals);

        var b = km.getBracket();
        assertEquals("S1W", b.getChampion().getName());
        assertEquals("S2W", b.getSilver().getName());
        List<Team> bronze = b.getBronzes();
        assertNotNull(bronze);
        java.util.Set<String> bronzeNames =
                new java.util.HashSet<>(java.util.Arrays.asList(
                        bronze.get(0).getName(), bronze.get(1).getName()));
        assertTrue(bronzeNames.contains("S1L"));
        assertTrue(bronzeNames.contains("S2L"));
    }


    //147. bảng k tồn tại- invalid
    @Test
    void TopHelper_BangKhongTonTai_ThrowException() {
        //thiếu gr A
        List<Group> groups = List.of(
                dummyGroup("B", "B1", "B2"),
                dummyGroup("C", "C1", "C2"),
                dummyGroup("D", "D1", "D2"),
                dummyGroup("E", "E1", "E2"),
                dummyGroup("F", "F1", "F2"),
                dummyGroup("G", "G1", "G2"),
                dummyGroup("H", "H1", "H2"),
                dummyGroup("X", "X1", "X2")
        );
        KnockoutStageManagement km = new KnockoutStageManagement(dummyTournament(groups));
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, km::seedRoundOf16FromGroups);
        assertTrue(ex.getMessage().toLowerCase().contains("thieu bang a"));
    }

    //148. top2<2- invalid
    @Test
    void TopHelper_Top2SizeNhoHon2_ThrowException() {
        Group ga = dummyGroupTop1Only("A", "A1"); //chỉ 1 đội
        List<Group> groups = List.of(
                ga,
                dummyGroup("B", "B1", "B2"),
                dummyGroup("C", "C1", "C2"),
                dummyGroup("D", "D1", "D2"),
                dummyGroup("E", "E1", "E2"),
                dummyGroup("F", "F1", "F2"),
                dummyGroup("G", "G1", "G2"),
                dummyGroup("H", "H1", "H2")
        );
        KnockoutStageManagement km = new KnockoutStageManagement(dummyTournament(groups));
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, km::seedRoundOf16FromGroups);
        assertTrue(ex.getMessage().toLowerCase().contains("bang a") &&
                ex.getMessage().toLowerCase().contains("top 2"));
    }

    //149. dsach null/ rỗng- invalid
    @Test
    void EnsureFinished_DanhSachNullHoacRong_ThrowException() {
        assertThrows(IllegalStateException.class, () -> callEnsureFinished(null, "Round of 16"));
        assertThrows(IllegalStateException.class, () -> callEnsureFinished(new ArrayList<>(), "Quarter-Finals"));
    }

    //150. chưa finish- invalid
    @Test
    void WinnerOf_TranChuaKetThuc_ThrowException() {
        Match m = unfinishedMatch(dummyTeam("A"), dummyTeam("B"));
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> callWinnerOf(m));
        assertTrue(ex.getMessage().toLowerCase().contains("chua ket thuc"));
    }

    //151. đúng đội thua- valid
    @Test
    void LoserOf_NhanDienDungDoiThua() {
        Team a = dummyTeam("Alpha");
        Team b = dummyTeam("Beta");
        Match m = finishedMatch(a, b, 0, 1); //Beta=win,alpha=loser
        Team loser = callLoserOf(m);
        assertEquals("Alpha", loser.getName());
    }
}
