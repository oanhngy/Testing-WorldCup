package com.worldcup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Substitution {
    private final Player playerIn;
    private final Player playerOut;
    private final int minute;
    private final Team team;
    private final Match match;

    private static final Map<Match, Map<Team, Integer>> SUB_COUNTS = new ConcurrentHashMap<>();
    private static final Map<Match, Map<Team, Set<Player>>> IN_HISTORY = new ConcurrentHashMap<>();

    private Substitution( Player in, Player out, int minute, Team team, Match match) {
        this.playerIn = in;
        this.playerOut = out;
        this.minute = minute;
        this.team = team;
        this.match = match;
    }

    //đky 1 lượt thay
    public static Substitution register(Player in, Player out, int minute, Team team, Match match) {
        validate(in, out, minute, team, match);
        bumpCount(match, team, 1);
        addInHistory(match, team, in);
        return new Substitution(in, out, minute, team, match);
    }

    //đky thay nhiều cầu thủ( 1-3)
    public static List<Substitution> registerBatch(List<SubRequest> batch) {
        if (batch == null || batch.isEmpty()) {
            throw new IllegalArgumentException("Danh sach thay nguoi khong duoc rong");
        }
        if (batch.size() < 1 || batch.size() > 3) {
            throw new IllegalArgumentException("Moi dot thay chi duoc 1-3 cau thu (BR10).");
        }

        Match m0 = batch.get(0).match;
        Team  t0 = batch.get(0).team;

        for (SubRequest r : batch) {
            if (r.match != m0 || r.team != t0) {
                throw new IllegalArgumentException("Tat ca luot trong batch phai cung tran va cung doi!");
            }
        }

        int used = getCount(m0, t0);
        if (used + batch.size() > 3) {
            throw new IllegalStateException("Vuot qua 3 luot thay cho doi trong tran nay!");
        }

        for (SubRequest r : batch) {
            validate(r.playerIn, r.playerOut, r.minute, r.team, r.match);
        }

        List<Substitution> created = new ArrayList<>(batch.size());
        bumpCount(m0, t0, batch.size());
        for (SubRequest r : batch) {
            addInHistory(m0, t0, r.playerIn);
            created.add(new Substitution(r.playerIn, r.playerOut, r.minute, r.team, r.match));
        }
        return created;
    }

    public static int getCount(Match match, Team team) {
        Objects.requireNonNull(match, "match null");
        Objects.requireNonNull(team,  "team null");
        Map<Team, Integer> map = SUB_COUNTS.get(match);
        return (map == null) ? 0 : map.getOrDefault(team, 0);
    }
    
    public static void resetForMatch(Match match) {
        if (match != null) {
            SUB_COUNTS.remove(match);
            IN_HISTORY.remove(match);
        }
    }

    //check thay người hợp lệ
    private static void validate(Player in, Player out, int minute, Team team, Match match) {
        Objects.requireNonNull(in, "playerIn khong duoc null");
        Objects.requireNonNull(out, "playerOut khong duoc null");
        Objects.requireNonNull(team, "team khong duoc null");
        Objects.requireNonNull(match, "match khong duoc null");

        if (in == out) {
            throw new IllegalArgumentException("playerIn va playerOut phai khac nhau!");
        }

        if (minute < 0 || minute > 120) {
            throw new IllegalArgumentException("Phut thay nguoi khong hop le!");
        }

        if (team != match.getTeamA() && team != match.getTeamB()) {
            throw new IllegalArgumentException("Team khong thuoc tran dau!");
        }

        if (match.isFinished()) {
            throw new IllegalStateException("Tran da ket thuc, khong the thay nguoi!");
        }

        if (getCount(match, team) >= 3) {
            throw new IllegalStateException("Vuot qua 3 luot thay cho doi trong tran nay!");
        }

        Set<Player> alreadyIn = IN_HISTORY
                .getOrDefault(match, Collections.emptyMap())
                .getOrDefault(team, Collections.emptySet());
        if (alreadyIn.contains(out)) {
            throw new IllegalStateException("Cau thu da duoc thay vao truoc do, khong duoc cho ra lai trong tran!");
        }

        if (alreadyIn.contains(in)) {
            throw new IllegalStateException("Cau thu nay da vao san, khong the vao lan nua!");
        }
    }

    private static void bumpCount(Match match, Team team, int delta) {
        SUB_COUNTS.computeIfAbsent(match, k -> new ConcurrentHashMap<>()).merge(team, delta, Integer::sum);
    }

    private static void addInHistory(Match match, Team team, Player in) {
        IN_HISTORY.computeIfAbsent(match, k -> new ConcurrentHashMap<>()).computeIfAbsent(team,  k -> new HashSet<>()).add(in);
    }


    //getter
    public Player getPlayerIn() {
        return playerIn;
    }

    public Player getPlayerOut() {
        return playerOut;
    }

    public int getMinute() {
        return minute;
    }

    public Team getTeam() {
        return team;
    }

    public Match getMatch() {
        return match;
    }

    public static class SubRequest {
        public final Player playerIn;
        public final Player playerOut;
        public final int minute;
        public final Team team;
        public final Match match;

        public SubRequest(Player playerIn, Player playerOut, int minute, Team team, Match match) {
            this.playerIn = playerIn;
            this.playerOut = playerOut;
            this.minute = minute;
            this.team = team;
            this.match = match;
        }
    }

}
