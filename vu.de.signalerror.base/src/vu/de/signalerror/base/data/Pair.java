package vu.de.signalerror.base.data;

public class Pair<T1, T2>
{
	private T1 first;

	private T2 second;

	public Pair(T1 first, T2 second)
	{
		this.first = first;
		this.second = second;
	}

	public T1 first( )
	{
		return first;
	}

	public T2 second( )
	{
		return second;
	}

	public boolean equals( Object obj )
	{
		if (obj == null)
		{
			return false;
		}
		if (!(obj instanceof Pair<?,?>))
		{
			return false;
		}

		Pair<?, ?> other = (Pair<?, ?>) obj;
		boolean result = true;
		result &= first == null ? other.first() == null : first.equals(other.first());
		result &= second == null ? other.second() == null : second.equals(other.second());
		return result;
	}

	public int hashCode( )
	{
		int result = 0;
		if (first == null && second == null)
		{
			return 0;
		}
		else if (first == null)
		{
			return second.hashCode();
		}

		return first.hashCode();
	}
}
