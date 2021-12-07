## Overview
Java implementation for TLM (time line manager language)

## Example

```javascript
import random;
import timer;

var player = {
    x: 0,
    y: 0,
    symbol: '@'
};

var enemies = [];

launch -> _ {
    for i in 0..10 {
        enemies.push({
            x: random.inRange(0, 10),
            y: random.inRange(0, 10),
            symbol: random.choice(['d', 'D', 'w', 'H']),
        });
    }

    timer.schedule(60, _: emit tick);
}

tick -> _ {
    // several events in emit are guarantied to run in the same thread
    emit moveObjects {newPos: (1, 2)}, render;
}

_ -> checkCollision player, enemies {
    for enemy in enemies {
        if player.x == enemy.x and player.y == enemy.y {
            return {
                enemy: enemy,
            }
        }
    }

    return {
        enemy: null,
    }
}

keyUp -> _ {
    // get input only once a frame
    // better idea is to keep last input and apply it in render function
    if e.timestamp - lastInputTimestamp < 60 {
        return;
    }

    lastInputTimestamp = e.timestamp;

    if event.key == 'ArrowRight' {
        if checkCollision({x: player.x + 1, y: player.y}, enemies).enemy == null {
            player.x = player.x + 1;
        }
    } elif event.key == 'ArrowUp' {
        player.y = player.y - 1;
    } elif event.key == 'ArrowDown' {
        player.y = player.y + 1;
    } elif event.key == 'ArrowLeft' {
        player.y = player.x - 1;
    }
}

_ -> renderEnemy x, y {
    for enemy in enemies {
        if enemy.x == x and enemy.y == y {
            return {
                symbol: enemy.symbol
            }
        }
    }

    return {
        symbol: null
    }
}

render -> _ {
    for y in 0..50 {
        for x in 0..50 {
            if player.x == x and player.y == y {
                print(player.symbol);
            } else {
                if renderEnemy(x, y).symbol != null {
                    print(renderEnemy(x, y).symbol);
                } else {
                    print('.');
                }
            }
        }
        print('\n');
    }
}
```