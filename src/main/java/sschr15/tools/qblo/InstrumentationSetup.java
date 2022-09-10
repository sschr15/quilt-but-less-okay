/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package sschr15.tools.qblo;

import com.sun.tools.attach.VirtualMachine;
import jdk.internal.misc.VM;
import org.jetbrains.annotations.Contract;

import java.io.ByteArrayInputStream;
import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.zip.GZIPInputStream;

@SuppressWarnings("unchecked")
public class InstrumentationSetup {
	/* (non-Javadoc)
	 * This field is final and set to null because it's modified at runtime.
	 * It uses `sschr15.tools.qblo.Utils#getNull()` to get the null value
	 * in order to avoid erroneous inlining.
	 */
	private static final Instrumentation instrumentation = Utils.getNull();

	@Contract("-> !null")
	public static Instrumentation getInstrumentation() {
		if (instrumentation == null) {
			try {
				Unsafe.jdk(); // Guarantees jdk.internal.misc is exported

				Path jarPath = Path.of("instrumentation.jar");
				if (!Files.exists(jarPath)) {
					byte[] bytes = Base64.getDecoder().decode(BASE_64_GZIPPED_AGENT_JAR);
					try (var bis = new ByteArrayInputStream(bytes); var gis = new GZIPInputStream(bis)) {
						Files.copy(gis, jarPath);
					}
				}

				System.setProperty("jdk.attach.allowAttachSelf", "true");
				MethodHandle savedPropsGetter = Unsafe.lookup().findStaticGetter(VM.class, "savedProps", Map.class);
				Map<String, String> savedProps = (Map<String, String>) savedPropsGetter.invokeExact();

				savedProps.put("jdk.attach.allowAttachSelf", "true");

				String pid = Long.toString(ProcessHandle.current().pid());
				VirtualMachine vm = VirtualMachine.attach(pid);
				vm.loadAgent(jarPath.toAbsolutePath().toString());
				vm.detach();

				ClassLoader system = ClassLoader.getSystemClassLoader();
				Class<?> clazz = Class.forName("sschr15.tools.qblo.agent.InstrumentationAgent", false, system);
				MethodHandle method = TrustedLookup.findStaticGetter(clazz, "instrumentation", Instrumentation.class);
				Instrumentation instrumentation = (Instrumentation) method.invokeExact();

				Field field = InstrumentationSetup.class.getDeclaredField("instrumentation");
				MemberAccess.setFieldValue(field, null, instrumentation);
			} catch (Throwable t) {
				Unsafe.sun().throwException(t);
				throw new RuntimeException(t);
			}
		}

		return instrumentation == null ? Utils.justThrow(new RuntimeException("instrumentation is null")) : instrumentation;
	}

	@SuppressWarnings("SpellCheckingInspection")
	private static final String BASE_64_GZIPPED_AGENT_JAR =
			"H4sIAAAAAAAA/wvwZmYRYeDg4GDY4MkayoAEOBlYGHxdQxx1Pf3c9P+dYmBgZgjw" +
			"ZucASTFBlQSANTMwcDCEe7GGesTxbJoM5L0HYpAoXLOvo5+nm2twiJ6v29xee+5D" +
			"BgJHvzvsc3aTFXqj4LE9SkaGpbDA/nY32yTvs7Hs+/cGFhaUVu+5/7uYgUsu8o0k" +
			"d7vP2sw7Xqv3KXD0lf1eceyYTpkzj/Dil8dsPGdcDtJeu0t70g07puLsGe3Rmdf8" +
			"ivrevJs+Q3bPjtvC3jODpyp8fRB76VWEgeMC+9u2E2wfdtgJ1Klt6LuiUDdTROHb" +
			"ziXxf1hAnuAC+YGhywM1BEB+LS5OzigyNNXHrYoPSVVJfn5OMR61whhqC5Ny8pE0" +
			"eKFFgiR2DYnpqXkl+ojgF90pGqr2S7E9DxgztSwMDMb4tHnmFZcUleYCmYklmfl5" +
			"jiBBveScxOLi3uBYf2FHEdvLejqx4tH7RAREAux2ta5bkOHDeVFBTS3XV2elY/yc" +
			"bc67N9xduzv6SvzDzfsPPD+2RID317Nm+1MV21nN/xo8TH12bqbx9znWxc+/z8+3" +
			"Z8h5vLBJ4JCRiiizREqH7KfkdbPvaL3W52pf9MKQJ09y2sNc45bwRz852gQmLlyx" +
			"g2lq4tnSbtH02uDvObdLvpazXr5WGRkx79+dItvlcUsnVLfl2ipOKHLt9ZuRqCmr" +
			"EZfzTc5fc0fXip4ZXYI9ZTfOVPYlTpVMOXx37bU9/brz14v2JDsdMxW9bui5i7H+" +
			"+JN13l+Yb5/8tCohIsazPN376OVY9iDnGWkzNq6tK12aGyekGl/kdlQnz6hnSuv3" +
			"EBHfdx1BbhWiu4982NF/bnbeXL5Ud5kZX1iqrJnvHpR5lqqzYkevNc/k/8qXJkte" +
			"21jrstV21wUeOZMLPAsq9FWSOxN1Gr5r9f2dJ+6jmNyaaHWjInK7zWKtMnsR17P7" +
			"BVlPX/iT5zXBddL7sxO6notmLTYV9XzBEzRV+grHSrvrxTvvdTzXPNkXu6hqceZh" +
			"oZtVWxXm2E5SSrF4/vFRYNv1c9eLzgrmHvRYHXh746TNr/0CbYxrToXsN/f7FWbg" +
			"v8/5S+GLLf8ZNSZpLJvv/Dg26uylMxlyF/wOSOk+suptzhXoiv9o+VV4odDZ6M71" +
			"E7bGB2xRa7N3zspYLnF6TZ9f63mDe0d630hNPidwi1+9yPdO1VfWwzPfR9yLt3X8" +
			"k95cdHSCSq9rS5SXVGbDGU8T95NJPcc4pZ4bOO0pfNSTxJzsIbjrYprbx7BjSS2H" +
			"qv+f0Q3YNSE/xntOkqmGS+bCySGnRD6+j59k71zzP82MMcr+B3eANyOTCANq4QMr" +
			"WUCFDypAKYpAWu0ZcBU9KlAtCkBsi6Mg4gLLMjAwMkgwPHaxdb224gYjjH74WUsT" +
			"RINs4WLAXTYgABMjckmBWxcfii4NRsySA7deYRS9IRh6oSUJwgBsZQkCtGI3AFay" +
			"IMIXs2xBDt89+IzBXdIgB/9G6+t37my9wcgkan0fRP8TgfADvFnZQGrYgVABaHk1" +
			"OEkAAN0x7ySxBgAA";
}
