package com.vonbraunz.botanypots.proxy;

import com.vonbraunz.botanypots.client.RenderBotanyPot;

public class ClientProxy extends CommonProxy {

    @Override
    public void registerRenderers() {
        RenderBotanyPot.register();
    }
}
