package me.theseems.tqueue;

public enum Verdict {
  OK(true, "Accepted"),
  FORBIDDEN(false, "Forbidden"),
  WHITELISTED(false, "Whitelisted"),
  ALREADY_AT(false, "Current server");

  public boolean ok;
  public String desc;

  Verdict(boolean ok, String desc) {
    this.ok = ok;
    this.desc = desc;
  }

  public void setDesc(String desc) {
    this.desc = desc;
  }
}
