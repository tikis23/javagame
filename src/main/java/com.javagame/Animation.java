package com.javagame;

final public class Animation {
    public Animation(int from, int to, int stepSize, double speed, boolean loop) {
        m_from = from;
        m_to = to;
        m_stepSize = stepSize;
        m_speed = speed;
        m_loop = loop;
        m_currentFrame = m_from;
        m_currentTime = 0;
    }
    public void step(double dt) {
        m_currentTime += m_speed * dt;
        if (m_currentTime >= 1000.0) {
            m_currentTime = 0;
            m_currentFrame += m_stepSize;
            if (m_currentFrame > m_to) {
                if (m_loop) {
                    m_currentFrame = m_from;
                } else {
                    m_currentFrame = m_to;
                }
            }
        }
    }
    public int getFrame() {
        return m_currentFrame;
    }
    public boolean isFinished() {
        return !m_loop && (m_currentFrame == m_to);
    }

    private int m_from;
    private int m_to;
    private int m_stepSize;
    private double m_speed;
    private boolean m_loop;
    private int m_currentFrame;
    private double m_currentTime;
}
