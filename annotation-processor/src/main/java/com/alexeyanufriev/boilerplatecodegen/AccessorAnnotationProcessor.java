package com.alexeyanufriev.boilerplatecodegen;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.HashSet;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class AccessorAnnotationProcessor extends AbstractProcessor {

    private JavacProcessingEnvironment processingEnvironment;
    private TreeMaker treeMaker;
    private Context context;
    private Trees trees;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        processingEnvironment = (JavacProcessingEnvironment) processingEnv;
        context = processingEnvironment.getContext();
        treeMaker = TreeMaker.instance(context);
        trees = Trees.instance(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if(annotations != null && !annotations.isEmpty()) {
            Elements elements = processingEnvironment.getElementUtils();
            TypeElement typeElement = elements.getTypeElement(Accessor.class.getCanonicalName());

            if(typeElement != null) {
                Set<? extends Element> fields = roundEnv.getElementsAnnotatedWith(typeElement);
                JavacElements utils = processingEnvironment.getElementUtils();

                for(Element field : fields) {
                    Accessor accessor = field.getAnnotation(Accessor.class);

                    if(accessor != null) {
                        JCTree node = utils.getTree(field);
                        if(node instanceof JCTree.JCVariableDecl) {

                            Name fieldName = ((JCTree.JCVariableDecl) node).getName();
                            JCTree.JCStatement returnStatement = treeMaker.Return(treeMaker.Ident(fieldName));
                            JCTree.JCBlock methodBody = treeMaker.Block(0, List.of(returnStatement));

                            Name methodName = createGetterMethodName(context, fieldName);
                            JCTree.JCExpression returnType = ((JCTree.JCVariableDecl) node).vartype;
                            List<JCTree.JCTypeParameter> generics = List.nil();
                            List<JCTree.JCVariableDecl> parameters = List.nil();
                            List<JCTree.JCExpression> exceptions = List.nil();
                            JCTree.JCExpression defaultValue = null;

                            JCTree.JCMethodDecl getterMethod = treeMaker.MethodDef(
                                    treeMaker.Modifiers(Flags.PUBLIC, List.nil()),
                                    methodName, returnType, generics, parameters, exceptions, methodBody, defaultValue);

                            TypeElement type = (TypeElement) field.getEnclosingElement();
                            ClassTree classTree = trees.getTree(type);
                            JCTree.JCClassDecl classDeclaration = (JCTree.JCClassDecl) classTree;
                            classDeclaration.defs = classDeclaration.defs.append(getterMethod);
                        }
                    }
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<>(1) {{
            add(Accessor.class.getCanonicalName());
        }};
    }

    private static Name createGetterMethodName(Context context, Name name) {
        String fieldName = name.toString();
        String methodName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        return (new Names(context)).fromString("get" + methodName);
    }
}
