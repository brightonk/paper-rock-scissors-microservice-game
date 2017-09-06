package org.microservice.game.web.websocket;

import java.util.Random;

/**
 * Class that perform various selection operation.
 *
 * @author Brighton Kukasira <brighton.kukasira@gmail.com>
 */
public interface Select {

  public static final Random rand = new Random();

  /**
   * A function to randomly select one of the items in a given list.
   *
   * @param params
   * @return
   */
  public static String random(String... params) {
    final int index = rand.nextInt(params.length);
    return params[index];
  }
}
