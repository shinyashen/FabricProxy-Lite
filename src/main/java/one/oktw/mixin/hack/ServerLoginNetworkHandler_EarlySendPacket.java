package one.oktw.mixin.hack;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.server.network.ServerLoginNetworkHandler;

import net.minecraft.text.Text;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ThreadLocalRandom;

import static one.oktw.VelocityLib.PLAYER_INFO_CHANNEL;
import static one.oktw.VelocityLib.PLAYER_INFO_PACKET;

@Mixin(ServerLoginNetworkHandler.class)
public class ServerLoginNetworkHandler_EarlySendPacket {
    public LoginHelloC2SPacket fallback;
    private boolean tag;
    @Shadow
    @Nullable GameProfile profile;

    @Shadow
    @Final
    ClientConnection connection;

    @Shadow
    public void onHello(LoginHelloC2SPacket p){
        
    };

    @Inject(method = "onHello", at = @At(value = "HEAD"), cancellable = true)
    private void skipKeyPacket(LoginHelloC2SPacket packet, CallbackInfo ci) {
        if(tag)return;
        if (profile != null && profile.isComplete()) return; // Already receive profile form velocity.
        if(packet==null){
            if(fallback!=null){
                tag=true;
                onHello(fallback);
            }else{
                connection.disconnect(Text.of("ERROR"));
            }
            ci.cancel();
            return;
        }
        fallback=packet;
        connection.send(new LoginQueryRequestS2CPacket(ThreadLocalRandom.current().nextInt(), PLAYER_INFO_CHANNEL, PLAYER_INFO_PACKET));
        ci.cancel();
    }
}
