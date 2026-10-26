#version 330 core

layout(location = 0) in vec2 aPos;
layout(location = 1) in vec2 aUv;
layout(location = 2) in vec4 aColor;

uniform mat4 uProjection;

out vec2 vUv;
out vec4 vColor;

void main() {
    // UVs da UI usam v para baixo; as texturas carregadas têm a linha 0 embaixo
    vUv = vec2(aUv.x, 1.0 - aUv.y);
    vColor = aColor;
    gl_Position = uProjection * vec4(aPos, 0.0, 1.0);
}
