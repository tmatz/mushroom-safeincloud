package jp.gr.java_conf.tmatz.mushroom_safeincloud;

public class EntryInfo implements Comparable<EntryInfo>
{
	public int id;
	public String title;
	
	public EntryInfo(int id, String title)
	{
		this.id = id;
		this.title = title;
	}
	
	public String toString()
	{
		return title;
	}

	@Override
	public int compareTo(EntryInfo another)
	{
		return this.toString().compareTo(another.toString());
	}
}
