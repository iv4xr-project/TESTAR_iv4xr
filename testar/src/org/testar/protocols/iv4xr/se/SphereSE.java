package org.testar.protocols.iv4xr.se;

import eu.iv4xr.framework.spatial.Vec3;

public class SphereSE {

	private double radius;
	private Vec3 position;

	public SphereSE(Vec3 position) {
		// SE blocks have a size of 2.5, radius is the half by default
		this.radius = 1.25;
		this.position = position;
	}

	public SphereSE(double radius, Vec3 position) {
		this.radius = radius;
		this.position = position;
	}

	public Vec3 getPosition() {
		return position;
	}

	// function to calculate if a 3D point is inside the sphere
	public boolean pointInsideSphere(float x, float y, float z) {
		double x1 = Math.pow((x - position.x), 2);
		double y1 = Math.pow((y - position.y), 2);
		double z1 = Math.pow((z - position.z), 2);

		return (x1 + y1 + z1) <= (radius * radius);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		if (!(o instanceof SphereSE)) {
			return false;
		}

		SphereSE s = (SphereSE) o;

		return this.position.equals(s.position);
	}

	@Override
	public int hashCode() {
		return this.position.hashCode();
	}

	/*private void calculateArea() {
		this.area = radius * radius * Math.PI;
	}

	public boolean pointInsideArea(int x, int y) {
		// Compare radius of circle with distance of its center from given point
		if ((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY) <= radius * radius) {
			return true;
		} else {
			return false;
		}
	}

	public String toString() {
		return "The area of the sphere [radius = " + radius + "]: " + area;
	}*/

}
