package org.hai.jhook.web;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import org.hai.jhook.bean.Result;
import org.hai.jhook.client.JHookClient;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
public class JHookController {

    @RequestMapping("/listJvm")
    public Result listJvm() {
        List<VirtualMachineDescriptor> list = VirtualMachine.list();
        List<Map<String, String>> result = list.stream().map(v -> {
            Map<String, String> data = new HashMap<>();
            data.put("id", v.id());
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
            throw new RuntimeException("unknown error");
        }
        VirtualMachineDescriptor desc = currentVmd.get();
        System.out.println("current desc: " + desc.displayName());
        VirtualMachine machine = VirtualMachine.attach(desc);
        machine.loadAgent("F:\\java-hook-ide\\jhook-agent\\target\\jhook-agent-1.0-jar-with-dependencies.jar");
        return Result.success();
    }

    @RequestMapping("/listClass")
    public Result listClass() throws Exception {
        JHookClient connect = JHookClient.connect();
        return Result.success().setData(connect.listClass());
    }

    @RequestMapping("/decompileClass/{className}")
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
        if (first.isPresent()) {
            return Result.success().setData(Files.readString(first.get().toPath()));
        }
        return Result.success().setData("error");
    }

    @RequestMapping("/listMethod/{className}")
    public Result listMethod(@PathVariable("className") String className) throws Exception {
        JHookClient connect = JHookClient.connect();
        return Result.success().setData(connect.listMethod(className));
    }

    @RequestMapping("/class/{className}")
    public Result getClassByteCode(@PathVariable("className") String className) throws Exception {
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

    @RequestMapping("/redefine/{className}/{methodName}/{position}/{line}")
    public Result redefineClass(@PathVariable("className") String className,
                                @PathVariable("methodName") String methodName,
                                @PathVariable("position") String position,
                                @PathVariable(value = "line", required = false) Integer line,
                                @RequestBody String code) throws Exception {
        JHookClient connect = JHookClient.connect();
        String result = connect.redefineClass(className, methodName, position, line, code);
        connect.disconnect();
        return Result.success().setData(result);
    }
}
