$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$output = Join-Path $root 'docs/presentation'
$app = New-Object -ComObject PowerPoint.Application
$deck = $app.Presentations.Add(0)
$deck.PageSetup.SlideWidth = 960
$deck.PageSetup.SlideHeight = 540
function Text($slide, $value, $x, $y, $w, $h, $size, $color) {
    $shape = $slide.Shapes.AddTextbox(1,$x,$y,$w,$h)
    $shape.TextFrame.TextRange.Text = $value
    $shape.TextFrame.TextRange.Font.Name = 'Segoe UI'
    $shape.TextFrame.TextRange.Font.Size = $size
    $shape.TextFrame.TextRange.Font.Color.RGB = $color
    return $shape
}
function Slide($title,$subtitle,$picture,$note) {
    $s = $deck.Slides.Add($deck.Slides.Count+1,12)
    $s.FollowMasterBackground = 0
    $s.Background.Fill.ForeColor.RGB = 2762511
    Text $s 'EVENTSPROJECT  /  MODULE TACHE' 30 12 850 25 11 13953630 | Out-Null
    Text $s $title 30 42 900 52 28 16777215 | Out-Null
    Text $s $subtitle 30 98 900 50 16 14803425 | Out-Null
    if ($picture) {
        $path = Join-Path $root "docs/evidence/$picture"
        $image = $s.Shapes.AddPicture($path,0,-1,100,156,760,396)
        # Fit inside the evidence area, preserving the screenshot's aspect ratio.
        $image.LockAspectRatio = -1
        $image.Height = 340
        $image.Left = (960-$image.Width)/2
        Text $s "Preuve : $picture" 30 504 850 22 9 13953630 | Out-Null
    }
    Text $s "$($deck.Slides.Count)" 912 507 30 20 10 14803425 | Out-Null
    $s.NotesPage.Shapes.Placeholders.Item(2).TextFrame.TextRange.Text = $note
    return $s
}
try {
    $s = Slide 'Des sources aux preuves' 'Contribution individuelle : affectation des rôles des participants' $null 'Le dépôt réel est eventsProject. Tache est un enum, pas une entité de tâches. Cette présentation couvre uniquement le diagnostic déjà exécuté. Les opérations métier et la chaîne CI/CD restent à réaliser.'
    Text $s "eventsProject`nBranche Tache" 45 180 850 110 38 16777215 | Out-Null
    Text $s "Diagnostic initial vérifié`nCompilation réussie · Connexion MySQL à corriger" 45 330 850 90 23 13953630 | Out-Null
    Text $s '29 octobre 2026  |  Présentation progressive — phase 1' 45 465 850 30 14 14803425 | Out-Null
    Slide 'Un périmètre respecté' 'Tache décrit un rôle ; Participant le persiste sous forme de chaîne.' '01-project-setup/setup-02-tache-model.png' 'Source actuelle affichée dans un visualiseur Windows puis capturée à l''écran. @Enumerated(EnumType.STRING) conserve le nom de la valeur dans la base. Renommer une valeur impose de considérer les données existantes. Aucune nouvelle logique métier n''a encore été ajoutée.' | Out-Null
    Slide 'Une branche de contribution dédiée' 'Branche locale Tache vérifiée ; historique conservé, aucun push effectué.' '05-git/git-01-branch-history.png' 'Les commandes Git ont été exécutées lors de la capture. Le commit de diagnostic ajoute la documentation et les journaux. La branche distante Tache était absente lors de l''inspection initiale. Cela ne démontre pas encore un Git Flow complet.' | Out-Null
    Slide 'Rendre les outils accessibles' 'Maven 3.9.14 et Java 8 sélectionnés pour la session PowerShell.' '02-maven/maven-01-toolchain.png' 'Commande exécutée au moment de la capture. Maven était déjà installé dans le cache mais absent du PATH. Le script modifie JAVA_HOME et PATH pour la session uniquement. Java 8 correspond à la cible du POM ; ce JDK ancien sert au diagnostic local.' | Out-Null
    Slide 'Compiler le projet existant' '12 sources compilées avec succès, sans modification du code applicatif.' '02-maven/maven-02-baseline-build.png' 'Capture de la lecture du journal sauvegardé de mvn -B compile. Ce n''est pas une nouvelle compilation au moment de la capture. BUILD SUCCESS prouve la compilation, pas les tests, la connexion JDBC ou le fonctionnement métier. Aucun test n''existait dans le dépôt initial.' | Out-Null
    Slide 'Identifier le véritable blocage' 'HTTP démarre, mais MySQL refuse le compte configuré : l''API retourne 500.' '01-project-setup/setup-03-database-blocker.png' 'Capture de journaux sauvegardés pendant le diagnostic. Le démarrage utilisait ddl-auto=none et désactivait la création automatique de base. Le serveur existant refuse root sans mot de passe. Solution prévue : base dédiée et configuration par environnement, encore non appliquée.' | Out-Null
    $s = Slide 'La suite : rétablir puis automatiser' 'Un résultat n''est validé qu''avec une exécution et une preuve.' $null 'Le fonctionnement métier reste bloqué. Priorité suivante : base MySQL dédiée, requête API réelle, puis service de rôles et tests. Jenkins, SonarQube, Nexus, Docker applicatif, Kubernetes, Terraform et monitoring ne sont pas encore démontrés. Terraform apply exige la revue du plan et l''accord utilisateur.'
    Text $s "VALIDÉ`nBranche · compilation · diagnostic · preuves" 45 175 870 85 24 13953630 | Out-Null
    Text $s "À CORRIGER`nConnexion MySQL et fonctionnement de l''API" 45 280 870 85 24 16777215 | Out-Null
    Text $s "ENSUITE`nRôles Tache → tests et logs → chaîne CI/CD" 45 385 870 85 24 14803425 | Out-Null
    $deck.SaveAs((Join-Path $output 'eventsProject-Tache-phase-01.pptx'),24)
    $preview = Join-Path $output 'preview'
    New-Item -ItemType Directory -Force $preview | Out-Null
    $deck.Export($preview,'PNG',1600,900)
    Write-Output "Saved editable presentation with $($deck.Slides.Count) slides."
} finally {
    $deck.Close()
    [Runtime.InteropServices.Marshal]::ReleaseComObject($deck) | Out-Null
    [Runtime.InteropServices.Marshal]::ReleaseComObject($app) | Out-Null
}
