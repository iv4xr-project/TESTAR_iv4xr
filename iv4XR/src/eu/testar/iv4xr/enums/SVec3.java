/***************************************************************************************************
 *
 * Copyright (c) 2021 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2021 Open Universiteit - www.ou.nl
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package eu.testar.iv4xr.enums;

import java.util.Objects;

import eu.iv4xr.framework.spatial.Vec3;

public class SVec3 implements java.io.Serializable {
	private static final long serialVersionUID = 639048627994203600L;

	public float x, y, z;

	public SVec3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public String toString() {
		return String.format("<%s,%s,%s>", x, y, z);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Vec3)) return false;
		Vec3 v = (Vec3) obj;
		return x == v.x && y == v.y && z == v.z;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y, z);
	}

	public static SVec3 labToSVec3(eu.iv4xr.framework.spatial.Vec3 vec3) {
		return new SVec3(vec3.x, vec3.y, vec3.z);
	}

	public static spaceEngineers.model.Vec3 labToSE(eu.iv4xr.framework.spatial.Vec3 vec3) {
		return new spaceEngineers.model.Vec3(vec3.x, vec3.y, vec3.z);
	}

	public static SVec3 seToSVec3(spaceEngineers.model.Vec3 vec3) {
		return new SVec3(vec3.getX(), vec3.getY(), vec3.getZ());
	}

	public static eu.iv4xr.framework.spatial.Vec3 seToLab(spaceEngineers.model.Vec3 vec3) {
		return new eu.iv4xr.framework.spatial.Vec3(vec3.getX(), vec3.getY(), vec3.getZ());
	}

}
