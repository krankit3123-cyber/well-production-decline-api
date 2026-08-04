package com.bp.decline.core.enums;

/**
 * Arps (1945) decline curve types.
 *
 * <ul>
 *   <li>EXPONENTIAL  — b = 0 : q(t) = qi * exp(-Di * t)</li>
 *   <li>HYPERBOLIC   — 0 < b < 1 : q(t) = qi / (1 + b * Di * t)^(1/b)</li>
 *   <li>HARMONIC     — b = 1 : q(t) = qi / (1 + Di * t)</li>
 * </ul>
 */
public enum DeclineType {
    EXPONENTIAL,
    HYPERBOLIC,
    HARMONIC
}
