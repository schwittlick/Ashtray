package com.testing;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import peasy.PeasyCam;
import processing.core.PApplet;
import processing.xml.XMLElement;
import toxi.geom.Sphere;
import toxi.geom.Vec3D;
import toxi.geom.mesh.WETriangleMesh;
import toxi.physics.VerletParticle;
import toxi.physics.VerletPhysics;
import toxi.physics.VerletSpring;
import toxi.physics.behaviors.GravityBehavior;
import toxi.physics.constraints.BoxConstraint;
import toxi.physics.constraints.SphereConstraint;
import toxi.processing.ToxiclibsSupport;


public class Ashtray extends PApplet {
	
	XMLElement el;

	int DIM = 60;
	int REST_LENGTH = 50;
	float STRENGTH = 0.05f;

	VerletPhysics physics;
	ToxiclibsSupport gfx;
	WETriangleMesh mesh;
	BoxConstraint ground;
	ArrayList spheres = new ArrayList();

	boolean isGouraudShaded = true;
	boolean showSpheres = false;

	PeasyCam cam;

	public void setup() {
		size(800, 600, OPENGL);
		gfx = new ToxiclibsSupport(this);
		cam = new PeasyCam(this, 500);
		sphereDetail(8);
		initPhysics();
	}

	void initPhysics() {
		physics = new VerletPhysics();
		physics.addBehavior(new GravityBehavior(new Vec3D(0, 0.1f, 0)));

		spheres.add(new SphereConstraint(new Sphere(new Vec3D(0, 0, 0), 400),
				false));
		//ground = new BoxConstraint(new AABB(new Vec3D(0, 320, 0), new Vec3D(
		//		1000, 50, 1000)));
		//ground.setRestitution(0);
		for (int y = 0, idx = 0; y < DIM; y++) {
			for (int x = 0; x < DIM; x++) {
				VerletParticle p = new VerletParticle(x * REST_LENGTH
						- (DIM * REST_LENGTH) / 2, -200, y * REST_LENGTH
						- (DIM * REST_LENGTH) / 2);
				if (y == 0 || y == DIM - 1) {
					p.lock();
				}
				physics.addParticle(p);
				if (x > 0) {
					VerletSpring s = new VerletSpring(p, physics.particles
							.get(idx - 1), REST_LENGTH, STRENGTH);
					physics.addSpring(s);
				}
				if (y > 0) {
					VerletSpring s = new VerletSpring(p, physics.particles
							.get(idx - DIM), REST_LENGTH, STRENGTH);
					physics.addSpring(s);
				}
				idx++;
			}
		}
		// add spheres as constraint to all particles
		for (Iterator i = spheres.iterator(); i.hasNext();) {
			SphereConstraint s = (SphereConstraint) i.next();
			VerletPhysics.addConstraintToAll(s, physics.particles);
		}
		// add ground as constraint to all particles
		//VerletPhysics.addConstraintToAll(ground, physics.particles);
	}

	public void draw() {
		background(0);
		lights();
		// update simulation
		physics.update();
		// update cloth mesh
		updateMesh();
		// draw mesh either flat shaded or smooth
		fill(255, 160, 0);
		noStroke();
		gfx.mesh(mesh, isGouraudShaded);
		if (showSpheres) {
			fill(255);
			for (Iterator<SphereConstraint> i = spheres.iterator(); i.hasNext();) {
				// create a copy of the sphere and reduce its radius
				// in order to avoid rendering artifacts
				Sphere s = new Sphere(i.next().sphere);
				s.x = mouseX;
				s.radius *= 0.99;
				gfx.sphere(s, 20);
			}
		}
		
	}

	public void keyPressed() {
		if (key == 'x') {
			mesh.saveAsSTL(sketchPath("cloth.stl"));
		}
		if (key == 'g') {
			isGouraudShaded = !isGouraudShaded;
		}
		if (key == 's') {
			showSpheres = !showSpheres;
		}
		if (key == 'r') {
			initPhysics();
		}
	}

	// iterates over all particles in the grid order
	// they were created and constructs triangles
	void updateMesh() {
		mesh = new WETriangleMesh();
		for (int y = 0; y < DIM - 1; y++) {
			for (int x = 0; x < DIM - 1; x++) {
				int i = y * DIM + x;
				VerletParticle a = physics.particles.get(i);
				VerletParticle b = physics.particles.get(i + 1);
				VerletParticle c = physics.particles.get(i + 1 + DIM);
				VerletParticle d = physics.particles.get(i + DIM);
				mesh.addFace(a, d, c);
				mesh.addFace(a, c, b);
			}
		}
	}

	
	/**
	 * xml writer example
	 */
	void tryXML(){
		 PrintWriter pw = createWriter("xmlFin.xml");
		 XMLElement root = new XMLElement("points");
		 
		 XMLElement xml = new XMLElement("pt");
		 xml.setFloat("x", 1.0f);
		 xml.setFloat("y", 2.0f);
		 root.addChild(xml);
		 
		 root.write(pw);
		 pw.flush();
		 pw.close();
		 
		
		
	}
}
