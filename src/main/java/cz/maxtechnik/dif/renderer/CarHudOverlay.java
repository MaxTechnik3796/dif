package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import cz.maxtechnik.dif.entity.vehicle.BaseCarEntity;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
public class CarHudOverlay{
	private static float smoothedSpeed=0F;
	private static long lastLapFinishTime=-1;
	private static long currentBestLap=-1;
	private static boolean wasOnLine=false;
	private static BaseCarEntity lastCar=null;
	public static void render(GuiGraphics gui){
		Minecraft mc=Minecraft.getInstance();
		if(mc.player==null||mc.options.hideGui||!(mc.player.getVehicle() instanceof BaseCarEntity entity)){
			lastLapFinishTime=-1;
			currentBestLap=-1;
			wasOnLine=false;
			lastCar=null;
			return;
		}
		if(lastCar!=entity){
			lastLapFinishTime=-1;
			currentBestLap=-1;
			wasOnLine=false;
			lastCar=entity;
		}
		smoothedSpeed+=(entity.getSpeedKmh()-smoothedSpeed)*0.2F;
		float steeringRaw=-entity.getCurrentSteering();
		float rpm=entity.getRPM(), rL=entity.getRedlineRPM(), fp=entity.getFuelPercent();
		int gear=entity.getCurrentGear();
		boolean blink=(System.currentTimeMillis()%150L)<75L, revLimit=rpm>=rL;
		int pW=180, pH=76, px=mc.getWindow().getGuiScaledWidth()/2-pW/2, py=mc.getWindow().getGuiScaledHeight()-pH-56;
		assert mc.level!=null;
		boolean onLine=mc.level.getBlockState(entity.blockPosition()).getBlock()==DifModBlocks.LAP_TIMER.get()||mc.level.getBlockState(entity.blockPosition().below()).getBlock()==DifModBlocks.LAP_TIMER.get();
		long now=System.currentTimeMillis();
		if(onLine&&!wasOnLine){
			if(lastLapFinishTime!=-1){
				long lap=now-lastLapFinishTime;
				if(lap>2000){
					if(currentBestLap==-1||lap<currentBestLap) currentBestLap=lap;
					lastLapFinishTime=now;
				}
			}else lastLapFinishTime=now;
		}
		wasOnLine=onLine;
		if(lastLapFinishTime!=-1){
			long curLap=now-lastLapFinishTime;
			String curStr=String.format("%02d:%02d.%02d",(curLap/60000)%60,(curLap/1000)%60,(curLap%1000)/10);
			gui.drawCenteredString(mc.font,"LAP: "+curStr,mc.getWindow().getGuiScaledWidth()/2,py-12,0xFFFFFFFF);
		}
		if(currentBestLap!=-1){
			String bestStr=String.format("%02d:%02d.%02d",(currentBestLap/60000)%60,(currentBestLap/1000)%60,(currentBestLap%1000)/10);
			gui.drawCenteredString(mc.font,"BEST: "+bestStr,mc.getWindow().getGuiScaledWidth()/2,py-24,0xFFFFDD00);
		}
		gui.fill(px,py,px+pW,py+pH,0xAA000000);
		gui.fill(px,py,px+pW,py+1,0x88FFFFFF);
		gui.fill(px,py+pH-1,px+pW,py+pH,0x88FFFFFF);
		gui.fill(px,py,px+1,py+pH,0x88FFFFFF);
		gui.fill(px+pW-1,py,px+pW,py+pH,0x88FFFFFF);
		for(int i=0;i<15;i++){
			int color=0x22FFFFFF, lx=mc.getWindow().getGuiScaledWidth()/2-(15*11-2)/2+i*11;
			if(revLimit) color=blink?0xFFFF1111:0x55FF1111;
			else if(rpm>=entity.getIdleRPM()+(i/14f)*(rL-entity.getIdleRPM()))
				color=i<5?0xFF00DD00:i<10?0xFFFF5500:0xFF4455FF;
			gui.fill(lx,py+7,lx+9,py+17,color);
			if(color!=0x22FFFFFF) gui.fill(lx,py+7,lx+9,py+8,0x44FFFFFF);
		}
		PoseStack poseStack=gui.pose();
		poseStack.pushPose();
		poseStack.translate((float)mc.getWindow().getGuiScaledWidth()/2,py+22,0);
		poseStack.scale(2f,2f,1f);
		gui.drawCenteredString(mc.font,gear==-1?"R":gear==0?"N":String.valueOf(gear),0,0,revLimit?(blink?0xFFFF1111:0xFFAA0000):gear==-1?0xFFFF4444:gear==0?0xFFFFDD00:0xFFFFFFFF);
		int colL0=steeringRaw>0.85f?0xFFFF1111:0xFF444444;
		int colL1=steeringRaw>0.50f?0xFFFFDD00:0xFF444444;
		int colL2=steeringRaw>0.15f?0xFF00DD00:0xFF444444;
		gui.drawCenteredString(mc.font,"<",-26,0,colL0);
		gui.drawCenteredString(mc.font,"<",-18,0,colL1);
		gui.drawCenteredString(mc.font,"<",-10,0,colL2);
		float rS=-steeringRaw;
		int colR0=rS>0.15f?0xFF00DD00:0xFF444444;
		int colR1=rS>0.50f?0xFFFFDD00:0xFF444444;
		int colR2=rS>0.85f?0xFFFF1111:0xFF444444;
		gui.drawCenteredString(mc.font,">",10,0,colR0);
		gui.drawCenteredString(mc.font,">",18,0,colR1);
		gui.drawCenteredString(mc.font,">",26,0,colR2);
		poseStack.popPose();
		gui.drawCenteredString(mc.font,(int)smoothedSpeed+" km/h",mc.getWindow().getGuiScaledWidth()/2,py+44,0xFFCCCCCC);
		gui.drawString(mc.font,"Fuel "+(int)(fp*100f)+"%",px+8,py+59,fp>0.5f?0xFF00CC00:fp>0.25f?0xFFFFDD00:0xFFFF4444);
		gui.drawCenteredString(mc.font,"BRAKE",mc.getWindow().getGuiScaledWidth()/2,py+59,mc.options.keyJump.isDown()?0xFFFF1111:0xFF444444);
		String angleText=Math.round(Math.abs(steeringRaw)*25)+"°";
		gui.drawString(mc.font,angleText,px+pW-8-mc.font.width(angleText),py+59,0xFFFFFFFF);
	}
}