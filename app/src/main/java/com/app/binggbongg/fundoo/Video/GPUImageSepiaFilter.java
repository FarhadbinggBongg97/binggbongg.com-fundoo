package com.app.binggbongg.fundoo.Video;

import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;

public class GPUImageSepiaFilter extends GPUImageFilter {

    private static final String FRAGMENT_SHADER =
            "precision mediump float;" +
                    "varying vec2 vTextureCoord;" +
                    "uniform lowp sampler2D sTexture;" +
                    "const highp vec3 weight = vec3(0.2125, 0.7154, 0.0721);" +
                    "void main() {" +
                    "   vec4 FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "   gl_FragColor.r = dot(FragColor.rgb, vec3(.393, .769, .189));\n" +
                    "   gl_FragColor.g = dot(FragColor.rgb, vec3(.349, .686, .168));\n" +
                    "   gl_FragColor.b = dot(FragColor.rgb, vec3(.272, .534, .131));\n" +
                    "}";

    public GPUImageSepiaFilter() {
        super(NO_FILTER_VERTEX_SHADER, FRAGMENT_SHADER);
    }
}
