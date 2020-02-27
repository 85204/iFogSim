const { spawn } = require('child_process');
const run = (i, mips, mean) => {
    return new Promise((res) => {
        let USE_ANOTHER_DEVICE = i;
        const ls = spawn('/Library/Java/JavaVirtualMachines/jdk-11.jdk/Contents/Home/bin/java', [
            "-javaagent:/Applications/IntelliJ IDEA.app/Contents/lib/idea_rt.jar=60749:/Applications/IntelliJ IDEA.app/Contents/bin",
            '-Dfile.encoding=UTF-8',
            `-Dtest=${USE_ANOTHER_DEVICE}`,
            `-Dmips=${mips}`,
            `-Dmean=${mean}`,
            '-classpath',
            '/Users/chenyong/Documents/iFogSim/out/production/iFogSim:/Users/chenyong/Documents/iFogSim/jars/json-simple-1.1.1.jar:/Users/chenyong/Documents/iFogSim/jars/cloudsim-3.0.3.jar:/Users/chenyong/Documents/iFogSim/jars/guava-18.0.jar:/Users/chenyong/Documents/iFogSim/jars/cloudsim-examples-3.0.3-sources.jar:/Users/chenyong/Documents/iFogSim/jars/cloudsim-3.0.3-sources.jar:/Users/chenyong/Documents/iFogSim/jars/commons-math3-3.5-bin.zip:/Users/chenyong/Documents/iFogSim/jars/cloudsim-examples-3.0.3.jar:/Users/chenyong/Documents/iFogSim/jars/commons-math3-3.5/commons-math3-3.5.jar',
            'org.fog.test.Configs',
        ]);
        let power;
        let delay;
        ls.stdout.on('data', (data) => {
//            console.log(data+'')
            if (data.toString().trim() === '') return;
            power = delay;
            delay = data;
        });

        ls.stderr.on('data', (data) => {
          console.log(`stderr: ${data}`);
        });

        ls.on('close', (code) => {
//            console.log(`child process exited with code ${code}`);
            res({
                power: +power.toString(), delay: +delay.toString()
            })
        });
    })

}

(async () => {
    for(const mean of [5, 6, 7, 8]) {
        console.log('\nmean: ' + mean);
        let mips = 1300;
        const normal = await run(0, 2000, mean);
        console.log('normal: ', normal.power.toFixed(3), normal.delay.toFixed(3))
        for (let m = mips; m < 1400; m += 100) {
            mips = m;
//            console.log(mips);
            const res = await run(1, mips, mean);
            console.log('multi: ', res.power.toFixed(3), res.delay.toFixed(3))
            const powerRate = (1 - res.power / normal.power) * 100;
            const delayRate = (1 - res.delay / normal.delay) * 100;
            console.log('energySaving: ', powerRate.toFixed(3),'delaySaving:', delayRate.toFixed(3))
            console.log('diff: ', (powerRate + delayRate).toFixed(3))
        }
    }
})()