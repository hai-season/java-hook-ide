package org.hai.jhook.web;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import org.hai.jhook.bean.Redefine;
import org.hai.jhook.bean.Result;
import org.hai.jhook.client.JHookClient;
import org.hai.jhook.exception.JHookException;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RequestMapping("/api")
@RestController
public class JHookController {
    private final Logger logger = LoggerFactory.getLogger(JHookController.class);

    @RequestMapping("/listJvm")
    public Result listJvm() {
        List<VirtualMachineDescriptor> list = VirtualMachine.list();
        List<Map<String, String>> result = list.stream().map(v -> {
            Map<String, String> data = new HashMap<>();
            data.put("pid", v.id());
            data.put("displayName", v.displayName());
            return data;
        }).collect(Collectors.toList());
        return Result.success().setData(result);
    }

    @RequestMapping("/attach/{pid}")
    public Result attach(@PathVariable("pid") String pid) throws Exception {
        List<VirtualMachineDescriptor> list = VirtualMachine.list();
        Optional<VirtualMachineDescriptor> currentVmd =
                list.stream().filter(v -> v.id().equals(pid)).findFirst();
        if (!currentVmd.isPresent()) {
            throw new JHookException(String.format("pid 为 %s 的进程不存在", pid));
        }
        VirtualMachineDescriptor desc = currentVmd.get();
        logger.info("current desc: {}", desc.displayName());
        VirtualMachine machine = VirtualMachine.attach(desc);
        machine.loadAgent("F:\\java-hook-ide\\jhook-agent\\target\\jhook-agent-1.0-jar-with-dependencies.jar");
        return Result.success();
    }

    @RequestMapping("/listClass")
    public Result listClass() throws Exception {
        JHookClient connect = JHookClient.connect();
        List<String> classes = connect.listClass();
        connect.disconnect();
        return Result.success().setData(classes);
    }

    @RequestMapping("/decompile/{className}")
    public Result decompileClass(@PathVariable("className") String className) throws Exception {
        JHookClient connect = JHookClient.connect();
        byte[] classByteCode = connect.getClassByteCode(className);
        File tempFile = File.createTempFile("jhook-decompile-tmp-file", className + ".class");
        tempFile.deleteOnExit();
        Files.write(Paths.get(tempFile.toURI()), classByteCode);

        File resultTempFile = File.createTempFile("jhook-decompile-result-tmp-file", className);
        resultTempFile.deleteOnExit();
        String dir = resultTempFile.getParent() + "./jhook-decompile-dist/";
        File file = new File(dir);
        file.mkdirs();
        ConsoleDecompiler.main(new String[] { tempFile.getAbsolutePath(), file.getAbsolutePath() });
        Optional<File> first = Arrays.stream(file.listFiles()).filter(File::isFile)
                .sorted(Comparator.comparing(File::lastModified).reversed())
                .findFirst();
        connect.disconnect();
        if (first.isPresent()) {
            return Result.success().setData(Files.readString(first.get().toPath()));
        }
        return Result.fail("").setData("decompile error");
    }

    @RequestMapping("/listMethod/{className}")
    public Result listMethod(@PathVariable("className") String className) throws Exception {
        JHookClient connect = JHookClient.connect();
        List<String> methods = connect.listMethod(className);
        connect.disconnect();
        return Result.success().setData(methods);
    }

    @RequestMapping("/getClassByteCode")
    public Result getClassByteCode(@RequestBody String className) throws Exception {
        JHookClient connect = JHookClient.connect();
        byte[] data = connect.getClassByteCode(className);
        CtClass ctClass = ClassPool.getDefault().makeClass(new ByteArrayInputStream(data));
        CtMethod[] methods = ctClass.getDeclaredMethods();
        StringBuilder sb = new StringBuilder();
        for (CtMethod method : methods) {
            sb.append(method.getName()).append("、");
        }
        connect.disconnect();
        return Result.success().setData(sb.toString());
    }

    @RequestMapping("/redefine")
    public Result redefineClass(@RequestBody Redefine redefine) throws Exception {
        JHookClient connect = JHookClient.connect();
        String result = connect.redefineClass(redefine.getClassName(), redefine.getMethodName(), redefine.getPosition(), redefine.getLine(), redefine.getCode());
        connect.disconnect();
        return Result.success().setData(result);
    }
}
