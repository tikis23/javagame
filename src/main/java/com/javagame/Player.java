package com.javagame;

import javafx.geometry.Point2D;
import java.util.EnumSet;

import com.javagame.bullets.Bullet;
import com.javagame.bullets.PistolBullet;
import com.javagame.bullets.PlasmaBullet;
import com.javagame.bullets.ShotgunBullet;
import com.javasl.Script;
import com.javasl.runtime.types.*;

final public class Player extends Entity {
    public Player(double x, double y, World world) {
        super(null, 1000, 0, 0, new Point2D(x, y), 0.05, 0.2, 0.2);
        m_speed = 0.002;
        m_sensitivity = 0.05;
        m_healTimer = 0;
        m_dir = new Point2D(1, 0).normalize();
        getRigidBody().setCollisionType(Physics.CollideMask.PLAYER);
        getRigidBody().collisionFlags = EnumSet.of(Physics.CollideMask.WALL, Physics.CollideMask.ENEMY,
                                                   Physics.CollideMask.ENEMY_BULLET);
        m_world = world;

        m_guns = new Gun[] {
            new Gun(Sprite.get("pistol.png", 128, true), 1, new Animation(0, 6, 1, 15, false, 5), () -> {
                Point2D perpDir = new Point2D(-getDir().getY(), getDir().getX());
                Point2D vel = m_dir.add(getRigidBody().getVelocity());
                Bullet bullet = new PistolBullet(getPos(), vel.add(perpDir.multiply((Math.random() - 0.5) * 0.05)), 0.05, 200);
                m_world.addEntity(bullet, true);
            }),
            new Gun(Sprite.get("chaingun.png", 128, true), 0, new Animation(0, 3, 1, 40, false, 1), () -> {
                Point2D perpDir = new Point2D(-getDir().getY(), getDir().getX());
                Point2D vel = m_dir.add(getRigidBody().getVelocity());
                Bullet bullet = new PistolBullet(getPos(), vel.add(perpDir.multiply((Math.random() - 0.5) * 0.1)), 0.1, 300);
                m_world.addEntity(bullet, true);
            }),
            new Gun(Sprite.get("plasmagun.png", 128, true), 3, new Animation(0, 3, 1, 4, false, 2), () -> {
                Point2D perpDir = new Point2D(-getDir().getY(), getDir().getX());
                Point2D vel = m_dir.add(getRigidBody().getVelocity());
                Bullet bullet = new PlasmaBullet(getPos(), vel.add(perpDir.multiply((Math.random() - 0.5) * 0.01)), 0.005, 800);
                bullet.setPierce(3);
                bullet.getRigidBody().setMaxSpeed(4.0);
                bullet.setAcceleration(1.02);
                m_world.addEntity(bullet, true);
            }),
            new Gun(Sprite.get("shotgun.png", 256, true), 1, new Animation(0, 9, 1, 13, false, 1), () -> {
                double spread = 0.1;
                Point2D perpDir = new Point2D(-getDir().getY(), getDir().getX());
                Point2D vel = m_dir.add(getRigidBody().getVelocity());
                Bullet bullet = new ShotgunBullet(getPos(), vel.add(perpDir.multiply((Math.random() - 0.5) * 0.01)), 0.15, 600);
                m_world.addEntity(bullet, true);
                bullet = new ShotgunBullet(getPos(), vel.add(perpDir.multiply(-spread).multiply((Math.random() * 0.5 + 0.75))), 0.15, 600);
                m_world.addEntity(bullet, true);
                bullet = new ShotgunBullet(getPos(), vel.add(perpDir.multiply(spread).multiply((Math.random() * 0.5 + 0.75))), 0.15, 600);
                m_world.addEntity(bullet, true);
            }),
        };
        m_currentGun = 0;
    }

    @Override public void update(Input input, double dt, World world) {
        m_deltaTime = dt;
        m_world = world;
        
        // heal
        m_healTimer += dt;
        if (m_healTimer >= 5000) {
            m_healTimer = 5000;
            setHealth(getHealth() + Math.min(1, (int)(0.5 * dt)));
        }

        // update gun timers
        for (int i = 0; i < m_guns.length; i++) {
            if (i == m_currentGun) {
                m_guns[i].update(dt);
            } else {
                m_guns[i].reset();
            }
        }

        // toggle autoplay
        if (input.isPressed("P")) {
            m_autoPlay = !m_autoPlay;
        }
        if (m_autoPlay) {
            // load correct script
            String newScriptName = m_world.getMapName() + ".jsl";
            if (m_script == null || !newScriptName.equals(m_scriptName)) {
                m_scriptName = newScriptName;
                m_script = loadScript(m_scriptName);
            }
            if (m_script == null) {
                m_autoPlay = false;
                return;
            }
            m_script.execute();
            if (m_script.isFinished()) {
                m_autoPlay = false;
                m_script = null;
                System.out.println("Script " + m_scriptName + " finished.");
            }
            return;
        } else {
            m_script = null;
        }

        // direction
        Point2D mousePos = input.getMousePos();
        if (m_mouseOld == null) {
            m_mouseOld = mousePos;
        }
        if (input.isHeld("MOUSE_SECONDARY")) {
            Point2D diff = mousePos.subtract(m_mouseOld).multiply(m_sensitivity);
            // dont care about Y axis (for now atleast)
            double angle = diff.getX() * m_sensitivity;
            m_dir = new Point2D(Math.cos(angle) * m_dir.getX() - Math.sin(angle) * m_dir.getY(),
                            Math.sin(angle) * m_dir.getX() + Math.cos(angle) * m_dir.getY());
        }
        m_mouseOld = mousePos;

        // position
        Point2D offset = new Point2D(0, 0);
        if (input.isHeld("W")) offset = offset.add(m_dir);
        if (input.isHeld("S")) offset = offset.subtract(m_dir);
        if (input.isHeld("A")) offset = offset.add(m_dir.getY(), -m_dir.getX());
        if (input.isHeld("D")) offset = offset.subtract(m_dir.getY(), -m_dir.getX());
        double multiplier = 1;
        if (input.isHeld("SHIFT")) multiplier = 3;

        if (offset.magnitude() > 0) {
            getRigidBody().addVelocity(offset.normalize().multiply(m_speed * multiplier * dt));
        }

        if (input.isPressed("DIGIT1")) {
            m_currentGun = 0;
        } else if (input.isPressed("DIGIT2")) {
            m_currentGun = 1;
        } else if (input.isPressed("DIGIT3")) {
            m_currentGun = 2;
        } else if (input.isPressed("DIGIT4")) {
            m_currentGun = 3;
        }

        // shooting
        if (input.isHeld("MOUSE_PRIMARY")) {
            m_guns[m_currentGun].shoot();
        }
    }
    public Point2D getPos() {
        return getRigidBody().getPosition();
    }
    public Point2D getDir() {
        return new Point2D(m_dir.getX(), m_dir.getY());
    }
    public void setPos(double x, double y) {
        getRigidBody().setPosition(new Point2D(x, y));
    }
    public void setDir(double x, double y) {
        m_dir = new Point2D(x, y).normalize();
    }
    public Sprite getGunSprite() {
        return m_guns[m_currentGun].getSprite();
    }
    public int getGunSpriteFrame() {
        return m_guns[m_currentGun].getSpriteFrame();
    }
    public void takeDamage(int damage) {
        setHealth(getHealth() - damage);
        m_healTimer = 0;
    }
    @Override public void onCollideWall() {}
    @Override public void onCollideEntity(Entity ent) {
        if (ent.getRigidBody().getCollisionType() != Physics.CollideMask.ENEMY_BULLET) return;
        takeDamage(ent.getDamage());
    }
    
    private Script loadScript(String name) {
        Script script = new Script();
        script.addDefaultFunctionPrint(false);
        addPlayerNativesToScript(script);

        try {
            script.compileFromFile("scripts/" + name);
        } catch (Exception e) {
            System.err.println("Failed to load script " + name + ": " + e.getMessage());
            return null;
        }
        if (!script.isReady()) {
            System.err.println("Script is not ready.");
            return null;
        }
        return script;
    }
    private void addPlayerNativesToScript(Script script) {
        /* Natives:
        wait()
        playerStepToExit()
        playerIsExitReached()
        worldGetEnemyCount()
        worldGetClosestEnemyId()
        playerCanShootEnemy(enemyId)
        playerCanSeeEnemy(enemyId)
        playerTurnToEnemy(enemyId)
        playerStepToEnemy(enemyId)
        playerAttack()
        playerGetWeaponCount()
        playerSetWeapon(weaponId)
        playerTurnToVelocityDir()
        playerIsWalking()
        playerMoveLeft(amount)
        playerMoveRight(amount)
        }
         */
        double moveSpeed = 6;
        Pathfinding pathFinder = new Pathfinding(10000);
        pathFinder.ignoreIfTargetIsBlocked(true);
        script.addExternalFunction("wait", true, new Void_T(), new Type_T[]{}, (p) -> {
            return new Void_T();
        });
        Point2D exit = new Point2D(m_world.getExits()[1].getX() + 0.5, m_world.getExits()[1].getY() + 0.5);
        script.addExternalFunction("playerStepToExit", false, new Void_T(), new Type_T[]{}, (p) -> {
            pathFinder.update(m_world, getPos(), exit, m_deltaTime * 500);
            Point2D[] path = pathFinder.getPath();
            if (path == null) return new Void_T();

            double speed = m_speed * moveSpeed * m_deltaTime;
            int startIndex = 0;
            for (int i = 0; i < path.length; i++) {
                if (getPos().distance(path[i]) < 0.5) {
                    startIndex = i + 1;
                    break;
                }
            }
            startIndex = Math.min(startIndex, path.length - 1);
            Point2D from = path[startIndex];
            if (startIndex + 1 < path.length) from = path[startIndex + 1];
            Point2D diff = from.subtract(getPos());
            diff = diff.normalize().multiply(speed);
            getRigidBody().addVelocity(diff);

            return new Void_T();
        });
        script.addExternalFunction("playerIsExitReached", false, new Bool_T(), new Type_T[]{}, (p) -> {
            return new Bool_T(getPos().distance(exit) < 0.5);
        });
        script.addExternalFunction("worldGetEnemyCount", false, new Int32_T(), new Type_T[]{}, (p) -> {
            return new Int32_T(m_world.getEnemyCount());
        });
        script.addExternalFunction("worldGetClosestEnemyId", false, new Int32_T(), new Type_T[]{}, (p) -> {
            return new Int32_T(m_world.getClosestEnemy(getPos(), true));
        });
        script.addExternalFunction("playerCanShootEnemy", false, new Bool_T(), new Type_T[]{new Int32_T()}, (p) -> {
            int enemyId = Script.intParam(p[0]);
            if (enemyId == -1) return new Bool_T(false);
            Point2D enemyPos = m_world.getEntities().get(enemyId).getRigidBody().getPosition();
            double dist = getPos().distance(enemyPos);
            Point2D enemyDir = enemyPos.subtract(getPos()).normalize();
            double hitDist = Pathfinding.castRay(m_world, getPos(), enemyDir);
            if (dist >= hitDist) return new Bool_T(false);

            // check dir
            double dot = m_dir.dotProduct(enemyDir);
            if (dot < 0.999) return new Bool_T(false);
            return new Bool_T(true);
        });
        script.addExternalFunction("playerCanSeeEnemy", false, new Bool_T(), new Type_T[]{new Int32_T()}, (p) -> {
            int enemyId = Script.intParam(p[0]);
            if (enemyId == -1) return new Bool_T(false);
            Point2D enemyPos = m_world.getEntities().get(enemyId).getRigidBody().getPosition();
            double dist = getPos().distance(enemyPos);
            Point2D enemyDir = enemyPos.subtract(getPos()).normalize();
            double hitDist = Pathfinding.castRay(m_world, getPos(), enemyDir);
            if (dist >= hitDist) return new Bool_T(false);
            return new Bool_T(true);
        });
        script.addExternalFunction("playerTurnToEnemy", false, new Void_T(), new Type_T[]{new Int32_T()}, (p) -> {
            int enemyId = Script.intParam(p[0]);
            if (enemyId == -1) return new Void_T();
            Point2D enemyPos = m_world.getEntities().get(enemyId).getRigidBody().getPosition();
            Point2D enemyDir = enemyPos.subtract(getPos()).normalize();
            m_dir = enemyDir;
            return new Void_T();
        });
        script.addExternalFunction("playerStepToEnemy", false, new Void_T(), new Type_T[]{new Int32_T()}, (p) -> {
            int enemyId = Script.intParam(p[0]);
            if (enemyId == -1) return new Void_T();
            Point2D enemyPos = m_world.getEntities().get(enemyId).getRigidBody().getPosition();

            pathFinder.update(m_world, getPos(), enemyPos, m_deltaTime * 500);
            Point2D[] path = pathFinder.getPath();
            if (path == null) return new Void_T();

            double speed = m_speed * moveSpeed * m_deltaTime;
            int startIndex = 0;
            for (int i = 0; i < path.length; i++) {
                if (getPos().distance(path[i]) < 0.5) {
                    startIndex = i + 1;
                    break;
                }
            }
            startIndex = Math.min(startIndex, path.length - 1);
            Point2D from = path[startIndex];
            if (startIndex + 1 < path.length) from = path[startIndex + 1];
            Point2D diff = from.subtract(getPos());
            diff = diff.normalize().multiply(speed);
            getRigidBody().addVelocity(diff);

            return new Void_T();
        });
        script.addExternalFunction("playerAttack", false, new Void_T(), new Type_T[]{}, (p) -> {
            m_guns[m_currentGun].shoot();
            return new Void_T();
        });
        script.addExternalFunction("playerGetWeaponCount", false, new Int32_T(), new Type_T[]{}, (p) -> {
            return new Int32_T(m_guns.length);
        });
        script.addExternalFunction("playerSetWeapon", false, new Void_T(), new Type_T[]{new Int32_T()}, (p) -> {
            int gunId = Script.intParam(p[0]);
            m_currentGun = gunId;
            return new Void_T();
        });
        script.addExternalFunction("playerTurnToVelocityDir", false, new Void_T(), new Type_T[]{}, (p) -> {
            m_dir = getRigidBody().getVelocity().normalize();
            return new Void_T();
        });
        script.addExternalFunction("playerIsWalking", false, new Bool_T(), new Type_T[]{}, (p) -> {
            return new Bool_T(getRigidBody().getVelocity().magnitude() > 0.05);
        });
        script.addExternalFunction("playerMoveLeft", false, new Void_T(), new Type_T[]{new Double_T()}, (p) -> {
            double amount = Script.doubleParam(p[0]);
            Point2D perpDir = new Point2D(m_dir.getY(), -m_dir.getX());
            getRigidBody().addVelocity(perpDir.normalize().multiply(m_speed * amount * moveSpeed * m_deltaTime));
            return new Void_T();
        });
        script.addExternalFunction("playerMoveRight", false, new Void_T(), new Type_T[]{new Double_T()}, (p) -> {
            double amount = Script.doubleParam(p[0]);
            Point2D perpDir = new Point2D(-m_dir.getY(), m_dir.getX());
            getRigidBody().addVelocity(perpDir.normalize().multiply(m_speed * amount * moveSpeed * m_deltaTime));
            return new Void_T();
        });
        script.addExternalFunction("random", false, new Int64_T(), new Type_T[]{new Int64_T(), new Int64_T()}, (p) -> {
            long min = Script.longParam(p[0]);
            long max = Script.longParam(p[1]);
            return new Int64_T((long)(Math.random() * (max - min) + min));
        });
        script.addExternalFunction("cheatHealPlayer", false, new Void_T(), new Type_T[]{}, (p) -> {
            setHealth(10000000);
            return new Void_T();
        });
        script.addExternalFunction("cheatKillEnemy", false, new Void_T(), new Type_T[]{new Int32_T()}, (p) -> {
            int enemyId = Script.intParam(p[0]);
            if (enemyId == -1) return new Void_T();
            Entity enemy = m_world.getEntities().get(enemyId);
            enemy.setHealth(-1000);
            return new Void_T();
        });
    }

    private double m_deltaTime = 0;
    private World m_world;
    private double m_healTimer;
    private int m_currentGun;
    private Gun[] m_guns;
    private Point2D m_dir;
    private double m_sensitivity;
    private double m_speed;
    private Point2D m_mouseOld;
    private boolean m_autoPlay = false;
    private Script m_script;
    private String m_scriptName;
}
