package com.worldcup;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class SimpleDB {
    private static final String DB_DIR = "data";
    private static final String URL = "jdbc:sqlite:" + DB_DIR + "/worldcup.db?foreign_keys=on";


    private SimpleDB() {}

    public static Connection get() throws SQLException {
        new File(DB_DIR).mkdirs();
        Connection c = DriverManager.getConnection(URL);
        try (Statement st = c.createStatement()) {
            st.execute("PRAGMA foreign_keys=ON");
        }
        return c;
    }


    public static void init() throws SQLException {
        try (Connection c = get(); Statement st = c.createStatement()) {
            st.execute("PRAGMA foreign_keys=ON");

            // TEAMS
            st.execute("""
                create table if not exists teams(
                  id           integer primary key autoincrement,
                  name         text not null,
                  region       text not null,
                  coach        text,
                  medical_staff text,
                  host         integer not null default 0,
                  points       integer not null default 0,
                  goal_diff    integer not null default 0,
                  unique(name, region)
                );
            """);

            // PLAYERS
            st.execute("""
                create table if not exists players(
                  id        integer primary key autoincrement,
                  team_id   integer not null,
                  name      text not null,
                  shirt_no  integer,
                  position  text,
                  yellows   integer not null default 0,
                  reds      integer not null default 0,
                  foreign key(team_id) references teams(id) on delete cascade
                );
            """);

            // GROUPS & GROUP_TEAMS (liên kết N-N)
            st.execute("""
                create table if not exists groups(
                  id   integer primary key autoincrement,
                  name text not null unique
                );
            """);
            st.execute("""
                create table if not exists group_teams(
                  group_id integer not null,
                  team_id  integer not null,
                  primary key (group_id, team_id),
                  foreign key(group_id) references groups(id) on delete cascade,
                  foreign key(team_id)  references teams(id) on delete cascade
                );
            """);

            // MATCHES
            st.execute("""
                create table if not exists matches(
                  id          integer primary key autoincrement,
                  group_id    integer,
                  team_a_id   integer not null,
                  team_b_id   integer not null,
                  goals_a     integer not null default 0,
                  goals_b     integer not null default 0,
                  status      text    not null default 'SCHEDULED', -- SCHEDULED/FINISHED
                  is_knockout integer not null default 0,
                  foreign key(group_id)  references groups(id) on delete set null,
                  foreign key(team_a_id) references teams(id),
                  foreign key(team_b_id) references teams(id)
                );
            """);

            // GOALS
            st.execute("""
                create table if not exists goals(
                  id           integer primary key autoincrement,
                  match_id     integer not null,
                  team_id      integer not null,
                  player_id    integer,
                  minute       integer not null,
                  own_goal     integer not null default 0,
                  foreign key(match_id) references matches(id) on delete cascade,
                  foreign key(team_id)  references teams(id),
                  foreign key(player_id) references players(id)
                );
            """);

            // SUBSTITUTIONS
            st.execute("""
                create table if not exists substitutions(
                  id            integer primary key autoincrement,
                  match_id      integer not null,
                  team_id       integer not null,
                  player_in_id  integer not null,
                  player_out_id integer not null,
                  minute        integer not null,
                  foreign key(match_id)      references matches(id) on delete cascade,
                  foreign key(team_id)       references teams(id),
                  foreign key(player_in_id)  references players(id),
                  foreign key(player_out_id) references players(id)
                );
            """);
        }
    }
}
