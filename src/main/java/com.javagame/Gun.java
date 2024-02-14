package com.javagame;

final public class Gun {
    interface ShootAction {
        void trigger();
    }
    public Gun(Sprite sprite, int shootFrameDelay, Animation anim, ShootAction action) {
        m_sprite = sprite;
        m_anim = anim;
        m_shooting = false;
        m_shootDelay = shootFrameDelay;
        m_action = action;
        m_shot = false;
    }
    public void update(double dt) {
        if (m_shooting) {
            if (!m_shot && m_anim.getFrame() == m_shootDelay) {
                m_action.trigger();
                m_shot = true;
            }
            m_anim.step(dt);
            if (m_anim.isFinished()) {
                reset();
            }
        }
    }
    public boolean shoot() {
        if (!m_shooting) {
            reset();
            m_shooting = true;
            return true;
        }
        return false;
    }
    public void reset() {
        m_shooting = false;
        m_shot = false;
        m_anim.reset();
    }
    public Sprite getSprite() {
        return m_sprite;
    }
    public int getSpriteFrame() {
        return m_anim.getFrame();
    }

    private boolean m_shot;
    private ShootAction m_action;
    private int m_shootDelay;
    private boolean m_shooting;
    private Animation m_anim;
    private Sprite m_sprite;
}
