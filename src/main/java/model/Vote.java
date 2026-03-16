package model;

/**
 * Rappresenta un voto assegnato da un giudice a un team.
 * Allineato con la tabella SQL 'vote' che ha chiave primaria composta (judgeId, teamId).
 */
public class Vote {
    private int judgeId;
    private int teamId;
    private int score;

    public Vote() {}

    public Vote(int judgeId, int teamId, int score) {
        this.judgeId = judgeId;
        this.teamId = teamId;
        this.score = score;
    }

    public int getJudgeId() { return judgeId; }
    public void setJudgeId(int judgeId) { this.judgeId = judgeId; }

    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}