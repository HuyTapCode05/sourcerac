package event.event_manifest;

/*
 *
 *
 * @author YourSoulMatee
 */

import boss.BossID;
import event.Event;

public class Christmas extends Event {

    @Override
    public void boss() {
        createBoss(BossID.ONG_GIA_NOEL, 30);
    }
}
