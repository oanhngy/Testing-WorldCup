package com.worldcup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Substitution_Test {
    private Team teamA, teamB, teamC;
    private Match match, match2;
    private Player pIn1, pOut1, pIn2, pOut2, pIn3, pOut3, pIn4, pOut4, pIn5, pOut5;

    @BeforeEach
    void setup() {
        teamA = mock(Team.class);
        teamB = mock(Team.class);
        teamC = mock(Team.class); //khác đội
        match  = mock(Match.class);
        match2 = mock(Match.class); //khác trận

        when(match.getTeamA()).thenReturn(teamA);
        when(match.getTeamB()).thenReturn(teamB);
        when(match.isFinished()).thenReturn(false);
        when(match2.getTeamA()).thenReturn(teamA);
        when(match2.getTeamB()).thenReturn(teamB);
        when(match2.isFinished()).thenReturn(false);

        pIn1  = mock(Player.class); pOut1 = mock(Player.class);
        pIn2  = mock(Player.class); pOut2 = mock(Player.class);
        pIn3  = mock(Player.class); pOut3 = mock(Player.class);
        pIn4  = mock(Player.class); pOut4 = mock(Player.class);
        pIn5  = mock(Player.class); pOut5 = mock(Player.class);

        Substitution.resetForMatch(match);
        Substitution.resetForMatch(match2);
    }

    @AfterEach
    void cleanup() {
        Substitution.resetForMatch(match);
        Substitution.resetForMatch(match2);
    }


    //152. minute<0- invalid
    @Test
    void SubstitutionRegister_MinuteDuoi0_ThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> Substitution.register(pIn1, pOut1, -1, teamA, match));
        assertEquals(0, Substitution.getCount(match, teamA));
    }

    //153. minute=0-valid
    @Test
    void SubstitutionRegister_Minute0_ThanhCong() {
        Substitution s = assertDoesNotThrow(
                () -> Substitution.register(pIn1, pOut1, 0, teamA, match));
        assertNotNull(s);
        assertEquals(0, s.getMinute());
        assertSame(teamA, s.getTeam());
        assertSame(match, s.getMatch());
        assertSame(pIn1, s.getPlayerIn());
        assertSame(pOut1, s.getPlayerOut());
        assertEquals(1, Substitution.getCount(match, teamA));
    }

    //154. valid minute
    @Test
    void SubstitutionRegister_MinuteGiuaTran_ThanhCong() {
        Substitution s = assertDoesNotThrow(
                () -> Substitution.register(pIn1, pOut1, 67, teamA, match));
        assertNotNull(s);
        assertEquals(67, s.getMinute());
        assertEquals(1, Substitution.getCount(match, teamA));
    }

    //155. minute>120- invalid
    @Test
    void SubstitutionRegister_MinuteHon120_ThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> Substitution.register(pIn1, pOut1, 121, teamA, match));
        assertEquals(0, Substitution.getCount(match, teamA));
    }

    //156. playernull- invalid
    @Test
    void SubstitutionRegister_playerInNull_ThrowException() {
        assertThrows(NullPointerException.class,
                () -> Substitution.register(null, pOut1, 10, teamA, match));
        assertEquals(0, Substitution.getCount(match, teamA));
    }

    //157. playerout null- invalid
    @Test
    void SubstitutionRegister_playerOutNull_ThrowException() {
        assertThrows(NullPointerException.class,
                () -> Substitution.register(pIn1, null, 10, teamA, match));
        assertEquals(0, Substitution.getCount(match, teamA));
    }

    //158. team null- invalid
    @Test
    void SubstitutionRegister_TeamNull_ThrowException() {
        assertThrows(NullPointerException.class,
                () -> Substitution.register(pIn1, pOut1, 10, null, match));

        assertEquals(0, Substitution.getCount(match, teamA));
    }

    //159. match null- invalid
    @Test
    void SubstitutionRegister_MatchNull_ThrowException() {
        assertThrows(NullPointerException.class,
                () -> Substitution.register(pIn1, pOut1, 10, teamA, null));
        assertEquals(0, Substitution.getCount(match, teamA));
    }

    //160. playerIn=playerOut- invalid
    @Test
    void SubstitutionRegister_PlayerInBangPlayerOut_ThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> Substitution.register(pIn1, pIn1, 15, teamA, match));
        assertEquals(0, Substitution.getCount(match, teamA));
    }

    //161. team k thuoc tran- invalid
    @Test
    void SubstitutionRegister_TeamKhongThuocTran_ThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> Substitution.register(pIn1, pOut1, 10, teamC, match));
        assertEquals(0, Substitution.getCount(match, teamA));
        assertEquals(0, Substitution.getCount(match, teamB));
    }

    //162. trận đã finish- invalid
    @Test
    void SubstitutionRegister_TranDaKetThuc_ThrowException() {
        when(match.isFinished()).thenReturn(true);
        assertThrows(IllegalStateException.class,
                () -> Substitution.register(pIn1, pOut1, 55, teamA, match));
        assertEquals(0, Substitution.getCount(match, teamA));
    }

    //163. vượt 3 lần thay/đội, lần 4 throw- invalid
    @Test
    void SubstitutionRegister_QuanHanMuc3_ThrowException() {
        //3 lần hợp lệ
        assertDoesNotThrow(() -> Substitution.register(pIn1, pOut1, 5,  teamA, match));
        assertDoesNotThrow(() -> Substitution.register(pIn2, pOut2, 60, teamA, match));
        assertDoesNotThrow(() -> Substitution.register(pIn3, pOut3, 75, teamA, match));
        assertEquals(3, Substitution.getCount(match, teamA));
        //lần 4-lỗi
        assertThrows(IllegalStateException.class,
                () -> Substitution.register(pIn4, pOut4, 85, teamA, match));
        assertEquals(3, Substitution.getCount(match, teamA));
    }

    //164. cầu thủ vào trước đó k đc ra lại- invalid
    @Test
    void SubstitutionRegister_LichSuVao_RaLai_ThrowException() {
        //cho pIn1 vào
        assertDoesNotThrow(() -> Substitution.register(pIn1, pOut1, 10, teamA, match));
        assertEquals(1, Substitution.getCount(match, teamA));
        //cho pIn1 RA sân- phải lỗi
        assertThrows(IllegalStateException.class,
                () -> Substitution.register(pIn2, pIn1, 20, teamA, match));
        assertEquals(1, Substitution.getCount(match, teamA));
    }

    //165. 1 cầu thủ k đc vào 2 lần- invalid
    @Test
    void SubstitutionRegister_Vao2LanLienTiep_ThrowException() {
        assertDoesNotThrow(() -> Substitution.register(pIn1, pOut1, 15, teamA, match));
        assertEquals(1, Substitution.getCount(match, teamA));

        assertThrows(IllegalStateException.class,
                () -> Substitution.register(pIn1, pOut2, 30, teamA, match));
        assertEquals(1, Substitution.getCount(match, teamA));
    }

    //166. VALID
    @Test
    void SubstitutionRegister_Valid_TraVeDoiTuongDung() {
        Substitution s = assertDoesNotThrow(() ->
                Substitution.register(pIn1, pOut1, 0, teamA, match));
        assertNotNull(s);
        assertSame(pIn1,  s.getPlayerIn());
        assertSame(pOut1, s.getPlayerOut());
        assertSame(teamA, s.getTeam());
        assertSame(match, s.getMatch());
        assertEquals(0, s.getMinute());
    }

    //167. check lượt tăng sau mỗi lần đky hợp lệ
    @Test
    void SubstitutionRegister_DemSauMoiLanTang() {
        assertEquals(0, Substitution.getCount(match, teamA));
        assertDoesNotThrow(() -> Substitution.register(pIn1, pOut1, 5,  teamA, match));
        assertEquals(1, Substitution.getCount(match, teamA));
        assertDoesNotThrow(() -> Substitution.register(pIn2, pOut2, 60, teamA, match));
        assertEquals(2, Substitution.getCount(match, teamA));
        assertDoesNotThrow(() -> Substitution.register(pIn3, pOut3, 75, teamA, match));
        assertEquals(3, Substitution.getCount(match, teamA));
    }

    //168. batch null/rỗng invalid
    @Test
    void SubstitutionRegisterBatch_BatchNullHoacRong_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> Substitution.registerBatch(null));
        assertThrows(IllegalArgumentException.class, () -> Substitution.registerBatch(new ArrayList<>()));
        assertEquals(0, Substitution.getCount(match, teamA));
    }

    //169. kích thước=0- invalid
    @Test
    void SubstitutionRegisterBatch_KickThuoc0_ThrowException() {
        List<Substitution.SubRequest> batch = Collections.emptyList();
        assertThrows(IllegalArgumentException.class, () -> Substitution.registerBatch(batch));
        assertEquals(0, Substitution.getCount(match, teamA));
    }

    //170. 4- invalid
    @Test
    void SubstitutionRegisterBatch_KichThuoc4_ThrowException() {
        List<Substitution.SubRequest> batch = Arrays.asList(
                new Substitution.SubRequest(pIn1, pOut1, 10, teamA, match),
                new Substitution.SubRequest(pIn2, pOut2, 11, teamA, match),
                new Substitution.SubRequest(pIn3, pOut3, 12, teamA, match),
                new Substitution.SubRequest(pIn4, pOut4, 13, teamA, match)
        );
        assertThrows(IllegalArgumentException.class, () -> Substitution.registerBatch(batch));
        assertEquals(0, Substitution.getCount(match, teamA));
    }

    //171. p.tử trận khác- invalid
    @Test
    void SubstitutionRegisterBatch_KhacTran_ThrowException() {
        List<Substitution.SubRequest> batch = Arrays.asList(
                new Substitution.SubRequest(pIn1, pOut1, 10, teamA, match),
                new Substitution.SubRequest(pIn2, pOut2, 11, teamA, match2) //khác match
        );
        assertThrows(IllegalArgumentException.class, () -> Substitution.registerBatch(batch));
        assertEquals(0, Substitution.getCount(match, teamA));
    }

    //172. p.tử khác đội- invalid
    @Test
    void SubstitutionRegisterBatch_KhacDoi_ThrowException() {
        List<Substitution.SubRequest> batch = Arrays.asList(
                new Substitution.SubRequest(pIn1, pOut1, 10, teamA, match),
                new Substitution.SubRequest(pIn2, pOut2, 11, teamC, match) //khác team
        );
        assertThrows(IllegalArgumentException.class, () -> Substitution.registerBatch(batch));
        assertEquals(0, Substitution.getCount(match, teamA));
        assertEquals(0, Substitution.getCount(match, teamC));
    }

    //173. vượt quota 3, hiện đang 2, thêm 2- invalid
    @Test
    void SubstitutionRegisterBatch_VuotQuota3_ThrowException() {
        //đã dùng 2 bằng register()
        assertDoesNotThrow(() -> Substitution.register(pIn1, pOut1, 5,  teamA, match));
        assertDoesNotThrow(() -> Substitution.register(pIn2, pOut2, 55, teamA, match));
        assertEquals(2, Substitution.getCount(match, teamA));
        //batch size= 2- vượt 3
        List<Substitution.SubRequest> batch = Arrays.asList(
                new Substitution.SubRequest(pIn3, pOut3, 70, teamA, match),
                new Substitution.SubRequest(pIn4, pOut4, 75, teamA, match)
        );
        assertThrows(IllegalStateException.class, () -> Substitution.registerBatch(batch));
        assertEquals(2, Substitution.getCount(match, teamA)); //đếm giữ nguyên= 2
    }

    //174. size=1- valid
    @Test
    void SubstitutionRegisterBatch_ValidSize1_Tao1BanGhi() {
        List<Substitution.SubRequest> batch = List.of(
                new Substitution.SubRequest(pIn1, pOut1, 0, teamA, match)
        );
        List<Substitution> created = assertDoesNotThrow(() -> Substitution.registerBatch(batch));
        assertNotNull(created);
        assertEquals(1, created.size());
        Substitution s = created.get(0);
        assertSame(pIn1,  s.getPlayerIn());
        assertSame(pOut1, s.getPlayerOut());
        assertSame(teamA, s.getTeam());
        assertSame(match, s.getMatch());
        assertEquals(0, s.getMinute());
        assertEquals(1, Substitution.getCount(match, teamA));
    }

    //175. size=3- valid
    @Test
    void SubstitutionRegisterBatch_ValidSize3_Tao3BanGhi() {
        List<Substitution.SubRequest> batch = Arrays.asList(
                new Substitution.SubRequest(pIn1, pOut1, 10, teamA, match),
                new Substitution.SubRequest(pIn2, pOut2, 10, teamA, match),
                new Substitution.SubRequest(pIn3, pOut3, 10, teamA, match)
        );
        List<Substitution> created = assertDoesNotThrow(() -> Substitution.registerBatch(batch));
        assertNotNull(created);
        assertEquals(3, created.size());
        assertEquals(3, Substitution.getCount(match, teamA));
    }

    //176. trong batch có phần invalid
    @Test
    void SubstitutionRegisterBatch_PhanTuInvalid_ThrowException() {
        List<Substitution.SubRequest> batch = Arrays.asList(
                new Substitution.SubRequest(pIn1, pOut1, 35,  teamA, match),
                new Substitution.SubRequest(pIn2, pOut2, 121, teamA, match) //k hợp lệ
        );
        assertThrows(IllegalArgumentException.class, () -> Substitution.registerBatch(batch));
        assertEquals(0, Substitution.getCount(match, teamA)); //k ghi nhận gì
    }




    //177. test getcount khi chưa đky
    @Test
    void GetCount_MatchTeamChuaCo_0() {
        assertEquals(0, Substitution.getCount(match, teamA));
        assertEquals(0, Substitution.getCount(match, teamB));
    }

    //178. match null- invalid
    @Test
    void GetCount_MatchNull_ThrowException() {
        assertThrows(NullPointerException.class, () -> Substitution.getCount(null, teamA));
    }

    //179. team null- invalid
    @Test
    void GetCount_TeamNull_ThrowException() {
        assertThrows(NullPointerException.class, () -> Substitution.getCount(match, null));
    }

    //180. test reset
    //ResetForMatch_XoaDemVaLichSu
    @Test
    void ResetForMatch_XoaDemVaLichSu() {
        //đăng ký 1 lần, pIn1 vào sân
        assertDoesNotThrow(() -> Substitution.register(pIn1, pOut1, 10, teamA, match));
        assertEquals(1, Substitution.getCount(match, teamA));
        //reset
        Substitution.resetForMatch(match);
        assertEquals(0, Substitution.getCount(match, teamA));
        // sau reset: lịch sử đã xoá, cho pIn2 vào và cho pIn1 RA sân là hợp lệ
        Substitution s = assertDoesNotThrow(() ->
                Substitution.register(pIn2, pIn1, 20, teamA, match));
        assertNotNull(s);
        assertEquals(1, Substitution.getCount(match, teamA));
    }

    //181. reset k ảnh hưởng match khác
    @Test
    void ResetForMatch_KhongAnhHuongTranKhac() {
        //đăng ký cho cả 2 trận
        assertDoesNotThrow(() -> Substitution.register(pIn1, pOut1, 5,  teamA, match));
        assertDoesNotThrow(() -> Substitution.register(pIn2, pOut2, 15, teamA, match2));
        assertEquals(1, Substitution.getCount(match,  teamA));
        assertEquals(1, Substitution.getCount(match2, teamA));
        //reset cho match
        Substitution.resetForMatch(match);
        // match bị reset-0; match2 giữ nguyên
        assertEquals(0, Substitution.getCount(match,  teamA));
        assertEquals(1, Substitution.getCount(match2, teamA));
    }

    //182. sai thứ tự kịch bản
    @Test
    void Register_SaiThuTuKichBan_HoanTronCacNhanh() {
        // 1/ thành công lần 1
        Substitution s1 = assertDoesNotThrow(() ->
                Substitution.register(pIn1, pOut1, 0, teamA, match));
        assertNotNull(s1);
        assertEquals(1, Substitution.getCount(match, teamA));
        // 2/ team không thuộc trận
        assertThrows(IllegalArgumentException.class, () ->
                Substitution.register(mock(Player.class), mock(Player.class), 10, teamC, match));
        assertEquals(1, Substitution.getCount(match, teamA));
        // 3/ thành công lần 2
        Substitution s2 = assertDoesNotThrow(() ->
                Substitution.register(pIn2, pOut2, 120, teamA, match));
        assertNotNull(s2);
        assertEquals(2, Substitution.getCount(match, teamA));
        // 4/ vào rồi lại ra 
        assertThrows(IllegalStateException.class, () ->
                Substitution.register(pIn3, pIn1, 70, teamA, match));
        assertEquals(2, Substitution.getCount(match, teamA));
        // 5/ thành công lần 3
        Substitution s3 = assertDoesNotThrow(() ->
                Substitution.register(pIn3, pOut3, 75, teamA, match));
        assertNotNull(s3);
        assertEquals(3, Substitution.getCount(match, teamA));
        // 6/ quá 3
        assertThrows(IllegalStateException.class, () ->
                Substitution.register(pIn4, pOut4, 80, teamA, match));
        assertEquals(3, Substitution.getCount(match, teamA));
    }

    //183. full quota= batch, reset, registerBatch tiếp ok
    @Test
    void RegisterBatch_SauReset_DangKyLaiThanhCong() {
        //quota 3 bằng 1 batch size=3
        List<Substitution.SubRequest> batch3 = Arrays.asList(
                new Substitution.SubRequest(pIn1, pOut1, 10, teamA, match),
                new Substitution.SubRequest(pIn2, pOut2, 10, teamA, match),
                new Substitution.SubRequest(pIn3, pOut3, 10, teamA, match)
        );
        List<Substitution> created3 = assertDoesNotThrow(() -> Substitution.registerBatch(batch3));
        assertNotNull(created3);
        assertEquals(3, created3.size());
        assertEquals(3, Substitution.getCount(match, teamA));
        //reset
        Substitution.resetForMatch(match);
        assertEquals(0, Substitution.getCount(match, teamA));
        //sau reset: batch size=2 đăng ký lại thành công
        List<Substitution.SubRequest> batch2 = Arrays.asList(
                new Substitution.SubRequest(pIn4, pOut4, 60, teamA, match),
                new Substitution.SubRequest(pIn5, pOut5, 65, teamA, match)
        );
        List<Substitution> created2 = assertDoesNotThrow(() -> Substitution.registerBatch(batch2));
        assertNotNull(created2);
        assertEquals(2, created2.size());
        assertEquals(2, Substitution.getCount(match, teamA));
    }

}
