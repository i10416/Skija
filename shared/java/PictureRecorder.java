package io.github.humbleui.skija;

import java.util.function.*;
import org.jetbrains.annotations.*;
import io.github.humbleui.skija.impl.*;
import io.github.humbleui.types.*;

public class PictureRecorder extends Managed {
    static { Library.staticLoad(); }

    @ApiStatus.Internal public Canvas _canvas;
    
    public PictureRecorder() {
        this(_nMake());
        Stats.onNativeCall();
    }

    @ApiStatus.Internal
    public PictureRecorder(long ptr) {
        super(ptr, _FinalizerHolder.PTR);
    }

    @ApiStatus.Internal
    public static class _FinalizerHolder {
        public static final long PTR = _nGetFinalizer();
    }

    /**
     * Returns the canvas that records the drawing commands.
     * 
     * @param bounds the cull rect used when recording this picture. Any drawing the falls outside
     *               of this rect is undefined, and may be drawn or it may not.
     * @return the canvas.
    */
    public Canvas beginRecording(Rect bounds) {
        assert _canvas == null : "Recording already in progress";
        try {
            Stats.onNativeCall();
            _canvas = new Canvas(_nBeginRecording(_ptr, bounds._left, bounds._top, bounds._right, bounds._bottom), false, this);
            return _canvas;
        } finally {
            ReferenceUtil.reachabilityFence(this);
        }
    }

    /**
     * @return  the recording canvas if one is active, or null if recording is not active.
     */
    @Nullable
    public Canvas getRecordingCanvas() {
        return _canvas;
    }

    /**
     * <p>Signal that the caller is done recording. This invalidates the canvas returned by
     * {@link #beginRecording(Rect)}/{@link #getRecordingCanvas()}.</p>
     *
     * <p>The returned picture is immutable. If during recording drawables were added to the canvas,
     * these will have been "drawn" into a recording canvas, so that this resulting picture will
     * reflect their current state, but will not contain a live reference to the drawables
     * themselves.</p>
     */
    public Picture finishRecordingAsPicture() {
        try {
            assert _canvas != null : "Recording not started";
            _canvas.invalidate();
            _canvas = null;
            Stats.onNativeCall();
            return new Picture(_nFinishRecordingAsPicture(_ptr));
        } finally {
            ReferenceUtil.reachabilityFence(this);
        }
    }

    /**
     * Signal that the caller is done recording, and update the cull rect to use for bounding
     * box hierarchy (BBH) generation. The behavior is the same as calling
     * {@link #finishRecordingAsPicture()}, except that this method updates the cull rect
     * initially passed into {@link #beginRecording(Rect)}.
     *
     * @param cull the new culling rectangle to use as the overall bound for BBH generation
     *             and subsequent culling operations.
     * @return the picture containing the recorded content.
     */
    public Picture finishRecordingAsPicture(@NotNull Rect cull) {
        try {
            assert _canvas != null : "Recording not started";
            _canvas.invalidate();
            _canvas = null;
            Stats.onNativeCall();
            return new Picture(_nFinishRecordingAsPictureWithCull(_ptr, cull._left, cull._top, cull._right, cull._bottom));
        } finally {
            ReferenceUtil.reachabilityFence(this);
        }
    }

    @Override
    public void close() {
        if (_canvas != null) {
            _canvas.invalidate();
            _canvas = null;
        }
        super.close();
    }

    // TODO
    /**
     * <p>Signal that the caller is done recording. This invalidates the canvas returned by
     * {@link #beginRecording(Rect)}/{@link #getRecordingCanvas()}.</p>
     *
     * <p>Unlike {@link #finishRecordingAsPicture()}, which returns an immutable picture,
     * the returned drawable may contain live references to other drawables (if they were added to
     * the recording canvas) and therefore this drawable will reflect the current state of those
     * nested drawables anytime it is drawn or a new picture is snapped from it (by calling 
     * {@link Drawable#makePictureSnapshot()}).</p>
     */
    // public Drawable finishRecordingAsPicture(@NotNull Rect cull) {
    //     Stats.onNativeCall();
    //     return new Drawable(_nFinishRecordingAsDrawable(_ptr, 0));
    // }

    @ApiStatus.Internal public static native long _nMake();
    @ApiStatus.Internal public static native long _nGetFinalizer();
    @ApiStatus.Internal public static native long _nBeginRecording(long ptr, float left, float top, float right, float bottom);
    @ApiStatus.Internal public static native long _nFinishRecordingAsPicture(long ptr);
    @ApiStatus.Internal public static native long _nFinishRecordingAsPictureWithCull(long ptr, float left, float top, float right, float bottom);
    @ApiStatus.Internal public static native long _nFinishRecordingAsDrawable(long ptr);
}