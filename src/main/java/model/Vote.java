package model;

public class Vote {
    private int voteId;      // Generato dal DB
    private int score;       // Il punteggio (es. da 1 a 10)
    private int teamId;      // A quale team è stato dato
    private int judgeId;     // Quale giudice lo ha assegnato
    private int hackthonId;

    public Vote(int judgeId, int teamId, int score) {
    }

    public Vote(int voteId, int score, int teamId, int judgeId, int hackthonId) {
        this.voteId = voteId;
        this.score = score;
        this.teamId = teamId;
        this.judgeId = judgeId;
        this.hackthonId = hackthonId;
    }

    public Vote(int score, int teamId, int judgeId, int hackthonId) {
        this.score = score;
        this.teamId = teamId;
        this.judgeId = judgeId;
        this.hackthonId = hackthonId;
    }

    // --- GETTER E SETTER ---

    public int getVoteId() { return voteId; }
    public void setVoteId(int voteId) { this.voteId = voteId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }

    public int getJudgeId() { return judgeId; }
    public void setJudgeId(int judgeId) { this.judgeId = judgeId; }

    public int gatHackathonId() { return hackthonId; }
    public void setHackathonId(int hackthonId) { this.hackthonId = hackthonId; }
}