package de.skawronek.compassnavigator;

public interface ITargetListListener {
	public void targetSelected(Target selectedTarget);

	public void targetAdded(Target newTarget);

	public void targetRemoved(Target removedTarget);
}
